---
name: error-handling
description: "Production-grade error handling patterns — error boundaries, error reporting (Sentry/GlitchTip/Rollbar), structured error responses (RFC 7807), graceful degradation, retry strategies with exponential backoff, user-facing error messages, and comprehensive logging. Use when implementing error handling in any application layer. Invoke for error boundaries, error reporting integration, structured API errors, retry/backoff logic, graceful degradation patterns, and error logging best practices."
license: MIT
compatibility: opencode
metadata:
  author: opencode
  version: "1.0.0"
  domain: backend
  triggers: error, exception, crash, retry, fallback, boundary, Sentry, logging, degrade, handling, recovery
  role: specialist
  scope: implementation
  output-format: code
---

# Error Handling

Production-grade error handling for every layer of the stack. Most code fails silently or crashes noisily — this skill fixes both.

## Core Principles

1. **Fail gracefully** — every error has a user-visible state that should make sense, not crash or blank screen.
2. **Log everything, expose nothing** — log full details internally, show minimal user-safe messages externally.
3. **Errors are data** — structured, typed, and machine-readable. Never throw strings.
4. **Recover when possible** — retry transient failures, fallback on stale data, degrade features before crashing.
5. **Every boundary needs handling** — API boundary, UI boundary, third-party integration boundary, database boundary.

## Error Taxonomy

```
Error
├── Operational (expected)
│   ├── Validation (user input wrong)
│   ├── Authentication (not logged in)
│   ├── Authorization (no permission)
│   ├── Not Found (resource doesn't exist)
│   ├── Conflict (duplicate, stale data)
│   ├── Rate Limited (too many requests)
│   └── External Service (downstream failed)
├── Transient (expected but temporary)
│   ├── Network Failure (timeout, DNS)
│   ├── Database Connection Lost
│   └── Remote Service Unavailable
└── Programming (unexpected — bug in code)
    ├── Null Reference
    ├── Type Error
    ├── Infinite Loop
    └── Memory Exhaustion
```

## Structured Error Responses (RFC 7807)

```json
// RFC 7807 Problem Details
{
  "type": "https://api.example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 422,
  "detail": "The request body contains invalid fields.",
  "instance": "/api/users",
  "errors": [
    {
      "field": "email",
      "message": "Must be a valid email address",
      "code": "INVALID_FORMAT"
    }
  ]
}
```

### HTTP status code usage

| Code | When | Body |
|------|------|------|
| 400 | Validation error, malformed request | Problem details with field errors |
| 401 | Missing or invalid auth | Problem details |
| 403 | Valid auth but insufficient permissions | Problem details |
| 404 | Resource not found | Problem details |
| 409 | Conflict (duplicate, stale version) | Problem details with conflicting data |
| 422 | Semantic validation (valid JSON, wrong values) | Problem details with field errors |
| 429 | Rate limited | Problem details + `Retry-After` header |
| 500 | Unexpected server error | Generic "Internal Server Error" — no details |
| 502 | Upstream service failed | Problem details |
| 503 | Service unavailable (maintenance, overload) | Problem details + `Retry-After` header |

## Error Reporting Integration

### Sentry/GlitchTip/Rollbar patterns

```typescript
// Structured error reporting
try {
  await processPayment(orderId, amount)
} catch (error) {
  // Tag with context for grouping
  Sentry.withScope((scope) => {
    scope.setTag('service', 'payments')
    scope.setTag('provider', paymentProvider)
    scope.setExtra('orderId', orderId)
    scope.setExtra('amount', amount)
    scope.setLevel('error')
    Sentry.captureException(error)
  })
}
```

### What to report

| Report | Don't report |
|--------|-------------|
| Programming errors (bugs) | Validation errors (expected) |
| External service failures | Auth failures (expected) |
| Unexpected states | 404s (user behavior) |
| Performance degraded | Rate limiting (system protection) |

### PII scrubbing

```typescript
// Before sending to error reporting
function scrubError(error: Error): Error {
  const message = error.message
    .replace(/[\w.-]+@[\w.-]+\.\w+/g, '[EMAIL]')
    .replace(/\b\d{4}[-\s]?\d{4}[-\s]?\d{4}[-\s]?\d{4}\b/g, '[CC]')
    .replace(/Bearer\s+[\w.-]+/g, 'Bearer [TOKEN]')
  return new Error(message)
}
```

## Retry Strategies

### Exponential backoff with jitter

```typescript
async function retry<T>(
  fn: () => Promise<T>,
  options: {
    maxRetries?: number
    baseDelay?: number
    maxDelay?: number
  } = {}
): Promise<T> {
  const { maxRetries = 3, baseDelay = 1000, maxDelay = 30000 } = options
  let lastError: Error | null = null

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      return await fn()
    } catch (error) {
      lastError = error as Error
      if (attempt === maxRetries) break
      if (!isRetryable(error)) throw error

      const delay = Math.min(
        baseDelay * Math.pow(2, attempt), // exponential
        maxDelay
      )
      const jitter = delay * (0.5 + Math.random() * 0.5) // add randomness
      await sleep(jitter)
    }
  }

  throw lastError
}
```

### Which errors are retryable

| Error type | Retryable? | Notes |
|-----------|-----------|-------|
| Network timeout | Yes | Transient |
| 429 (Rate limited) | Yes | Honor `Retry-After` header |
| 502/503/504 | Yes | Upstream unavailable |
| 5xx (generic) | Maybe | Could be transient or permanent |
| 400 (Bad request) | No | Won't succeed on retry |
| 401/403 | No | Auth won't change |
| 404 | No | Resource doesn't exist |
| 409 | No | Need new data, not retry |

### Retry budget

- Max 3 retries for most operations
- Max 1 retry for user-facing requests (don't make the user wait)
- Unlimited retries (with backoff) for background jobs
- Track retry count per request; stop manually after N

## Graceful Degradation

### Feature degradation levels

```
Level 0: Full functionality (all services available)
Level 1: Minor features disabled (recommendations, personalization)
Level 2: Write operations disabled (read-only mode)
Level 3: Core functionality only (authentication + basic reads)
Level 4: Maintenance page (all features disabled)
```

### Circuit breaker pattern

```typescript
interface CircuitBreakerState {
  failures: number
  lastFailureTime: number
  state: 'closed' | 'open' | 'half-open'
}

// CLOSED → failures >= threshold → OPEN
// OPEN → timeout elapsed → HALF-OPEN
// HALF-OPEN → success → CLOSED
// HALF-OPEN → failure → OPEN (with longer timeout)
```

### Fallback patterns

| Service | Fallback |
|---------|----------|
| Recommendation engine | Show popular defaults |
| Payment gateway | Switch to backup gateway |
| Search service | Basic SQL LIKE query |
| Image CDN | Serve from origin with compression disabled |
| Auth service | Degrade to read-only, force re-login on write |
| Database replica | Promote read replica to primary |

## Error Boundaries

### React error boundaries

```typescript
// Component level
<ErrorBoundary fallback={<ErrorFallback />}>
  <ExpensiveWidget />
</ErrorBoundary>

// Route level
<ErrorBoundary fallback={<RouteError />}>
  <Outlet />
</ErrorBoundary>

// Global level
<ErrorBoundary fallback={<AppCrashScreen onReset={() => navigate('/')} />}>
  <App />
</ErrorBoundary>
```

### Vue error boundaries

```html
<ErrorBoundary fallback="error-boundary">
  <ExpensiveWidget />
</ErrorBoundary>
```

Use `onErrorCaptured` for granular handling.

### Angular error handling

```typescript
// Global
class GlobalErrorHandler implements ErrorHandler {
  handleError(error: any): void {
    reportError(error)
    showUserNotification('Something went wrong. We've been notified.')
  }
}
```

### Error boundary best practices

1. **Granular boundaries** — catch errors as close to the source as possible. A crash in the sidebar should not take down the entire page.
2. **Recovery mechanism** — every boundary should have a "Try again" or "Go back" action.
3. **Log boundary activations** — if an error boundary catches something, report it to your error service.
4. **Reset state on retry** — when the user clicks "Try again", reset the component state entirely.

## User-Facing Error Messages

### Message tiers

| Tier | Where | Content |
|------|-------|---------|
| 1 | Inline (field-level) | Specific, actionable, brief |
| 2 | Toast/notification | Operational errors (network, server) |
| 3 | Inline banner | Section-level failures |
| 4 | Full page | Catastrophic failures (app crashed, maintenance) |

### Message structure

```
[What went wrong] + [Why it might have happened] + [What to do about it]
```

Examples:
- "Could not save your changes. Your internet connection may be unstable. [Retry]"
- "This page is temporarily unavailable. We're working on it. [Refresh] [Go to home]"
- "Payment failed. Your card was declined. [Try a different card]"

### Never show

- Stack traces
- SQL queries
- Internal service names or IP addresses
- File paths
- Raw error codes (show friendly error codes like "ERR-1234" for support)

## Logging

### Structured log format

```json
{
  "timestamp": "2026-05-20T10:30:00Z",
  "level": "error",
  "logger": "payment-service",
  "message": "Payment processing failed",
  "error": {
    "type": "ExternalServiceError",
    "code": "PROVIDER_TIMEOUT",
    "message": "Stripe gateway timed out after 30s"
  },
  "context": {
    "orderId": "ORD-12345",
    "amount": 49.99,
    "currency": "USD",
    "provider": "stripe",
    "attempt": 2
  },
  "request": {
    "method": "POST",
    "path": "/api/orders/12345/pay",
    "requestId": "req_abc123"
  },
  "user": {
    "id": "user_456"
  }
}
```

### Log levels

| Level | When | Examples |
|-------|------|----------|
| DEBUG | Development only, never in prod | Variable values, function entry/exit |
| INFO | Normal operation | Request received, order created, job started |
| WARN | Unexpected but handled | Retry attempt, fallback used, degraded mode |
| ERROR | Operational failure | External service down, validation failure |
| FATAL | Unrecoverable | DB connection lost, out of memory |

### What to log

| Log | Don't log |
|-----|-----------|
| Error type, message, stack trace | Full request bodies with PII |
| Request ID for correlation | Passwords, tokens, API keys |
| User ID (not email) | Credit card numbers |
| Service name, version | Session tokens |
| Duration, status code | Private keys |

## Framework-Specific Patterns

### Express/Fastify (Node.js)

```typescript
// Global error middleware
app.use((err: Error, req: Request, res: Response, next: NextFunction) => {
  if (err instanceof ValidationError) {
    return res.status(422).json(err.toProblemDetails())
  }
  if (err instanceof AuthError) {
    return res.status(401).json(err.toProblemDetails())
  }

  logger.error({ err, reqId: req.id })
  reportError(err, { requestId: req.id })

  res.status(500).json({
    type: 'https://api.example.com/errors/internal',
    title: 'Internal Server Error',
    status: 500,
    detail: 'Something went wrong. Our team has been notified.'
  })
})
```

### FastAPI (Python)

```python
@app.exception_handler(ValidationError)
async def validation_handler(request, exc):
    return JSONResponse(
        status_code=422,
        content=exc.to_problem_details()
    )

@app.exception_handler(Exception)
async def global_handler(request, exc):
    logger.error("Unhandled error", exc_info=exc, extra={"request_id": request.state.id})
    report_error(exc, request)
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"}
    )
```

### Spring Boot (Java)

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(ValidationException ex) {
        return ResponseEntity.unprocessableEntity()
            .body(ex.toProblemDetail());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGlobal(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for request: {}", request.getRequestId(), ex);
        return ResponseEntity.internalServerError()
            .body(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));
    }
}
```
