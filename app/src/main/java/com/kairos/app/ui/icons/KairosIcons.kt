package com.kairos.app.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Optimized Icon System for Kairos App
 *
 * This object provides efficient access to Material Design icons and custom icons.
 */
object KairosIcons {
    val SportsEsports: ImageVector = Icons.Filled.SportsEsports
    
    // === CORE MATERIAL ICONS (Pre-loaded) ===
    
    val Add: ImageVector = Icons.Filled.Add
    val ArrowBack: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val ArrowForward: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
    val BarChart: ImageVector = Icons.AutoMirrored.Filled.TrendingUp // Fallback for Stats
    val ChevronRight: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight
    val KeyboardArrowRight: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight
    val List: ImageVector = Icons.AutoMirrored.Filled.List
    val Send: ImageVector = Icons.AutoMirrored.Filled.Send
    
    val Check: ImageVector = Icons.Filled.Check
    val Close: ImageVector = Icons.Filled.Close
    val Delete: ImageVector = Icons.Filled.Delete
    val Done: ImageVector = Icons.Filled.Done
    val Edit: ImageVector = Icons.Filled.Edit
    val Email: ImageVector = Icons.Filled.Email
    val Favorite: ImageVector = Icons.Filled.Favorite
    val FavoriteBorder: ImageVector = Icons.Filled.FavoriteBorder
    val Home: ImageVector = Icons.Filled.Home
    val Info: ImageVector = Icons.Filled.Info
    val Menu: ImageVector = Icons.Filled.Menu
    val MoreVert: ImageVector = Icons.Filled.MoreVert
    val Person: ImageVector = Icons.Filled.Person
    val Phone: ImageVector = Icons.Filled.Phone
    val Place: ImageVector = Icons.Filled.Place
    val PlayArrow: ImageVector = Icons.Filled.PlayArrow
    val Search: ImageVector = Icons.Filled.Search
    val Settings: ImageVector = Icons.Filled.Settings
    val Share: ImageVector = Icons.Filled.Share
    val Star: ImageVector = Icons.Filled.Star
    val Warning: ImageVector = Icons.Filled.Warning
    val Mic: ImageVector = Icons.Filled.Mic
    val MicNone: ImageVector = Icons.Filled.MicNone
    val Spa: ImageVector = Icons.Filled.Spa
    val HealthAndSafety: ImageVector = Icons.Filled.HealthAndSafety
    val GridView: ImageVector = Icons.Filled.GridView
    val Image: ImageVector = Icons.Filled.Image
    val Stop: ImageVector = Icons.Filled.Stop
    val Pause: ImageVector = Icons.Filled.Pause
    
    // Missing Core Icons
    val Schedule: ImageVector = Icons.Filled.DateRange
    val LocalFireDepartment: ImageVector = Icons.Filled.LocalFireDepartment
    val EmojiEvents: ImageVector = Icons.Filled.EmojiEvents
    val Psychology: ImageVector = Icons.Filled.Psychology
    val Chat: ImageVector = Icons.AutoMirrored.Filled.Chat
    val Notifications: ImageVector = Icons.Filled.Notifications
    val Inbox: ImageVector = Icons.Filled.Inbox
    val Refresh: ImageVector = Icons.Filled.Refresh
    val School: ImageVector = Icons.Filled.School
    val Book: ImageVector = Icons.Filled.Book
    val Stars: ImageVector = Icons.Filled.Stars
    val TrendingUp: ImageVector = Icons.AutoMirrored.Filled.TrendingUp
    val TrendingDown: ImageVector = Icons.AutoMirrored.Filled.TrendingDown
    val ExpandLess: ImageVector = Icons.Filled.ExpandLess
    val ExpandMore: ImageVector = Icons.Filled.ExpandMore
    val MenuBook: ImageVector = Icons.AutoMirrored.Filled.MenuBook
    val ErrorOutline: ImageVector = Icons.Filled.ErrorOutline
    val AutoStories: ImageVector = Icons.AutoMirrored.Filled.MenuBook // Fallback
    val FormatQuote: ImageVector = Icons.Filled.FormatQuote
    val WbSunny: ImageVector = Icons.Filled.WbSunny
    val NightsStay: ImageVector = Icons.Filled.NightsStay
    val DarkMode: ImageVector = Icons.Filled.DarkMode
    val Bookmark: ImageVector = Icons.Filled.Bookmark
    val BookmarkBorderIcon: ImageVector = Icons.Filled.BookmarkBorder
    val LocalFlorist: ImageVector = Icons.Filled.LocalFlorist
    val Bolt: ImageVector = Icons.Filled.Bolt
    val History: ImageVector = Icons.Filled.History
    val Celebration: ImageVector = Icons.Filled.Celebration
    val SkipNext: ImageVector = Icons.Filled.SkipNext
    val Verified: ImageVector = Icons.Filled.Verified
    val Palette: ImageVector = Icons.Filled.Palette
    val Timer: ImageVector = Icons.Filled.Timer
    val ThumbUp: ImageVector = Icons.Filled.ThumbUp
    val Error: ImageVector = Icons.Filled.Error
    val Wallpaper: ImageVector = Icons.Filled.Wallpaper
    val HourglassEmpty: ImageVector = Icons.Filled.HourglassEmpty
    val Flag: ImageVector = Icons.Filled.Flag
    val Code: ImageVector = Icons.Filled.Code
    val Lock: ImageVector = Icons.Filled.Lock
    
    // Fallbacks for missing icons in standard set
    val HourglassBottom: ImageVector = Icons.Filled.HourglassEmpty 
    val Shuffle: ImageVector = Icons.Filled.Refresh 
    val Quiz: ImageVector = Icons.AutoMirrored.Outlined.Help 
    val TextFields: ImageVector = Icons.AutoMirrored.Filled.List 
    val Inventory2: ImageVector = Icons.Filled.Home 
    val SwapHoriz: ImageVector = Icons.AutoMirrored.Filled.List 
    val Analytics: ImageVector = Icons.Filled.DateRange
    val AlternateEmail: ImageVector = Icons.Filled.Email
    val Contacts: ImageVector = Icons.Filled.Person
    val MoreHoriz: ImageVector = Icons.Filled.MoreVert
    val CalendarMonth: ImageVector = Icons.Filled.DateRange
    val Assignment: ImageVector = Icons.AutoMirrored.Filled.List
    val Token: ImageVector = Icons.Filled.Star
    val HelpOutline: ImageVector = Icons.AutoMirrored.Filled.Help
    val Loop: ImageVector = Icons.Filled.Refresh
    val Tag: ImageVector = Icons.AutoMirrored.Filled.List
    val Circle: ImageVector = Icons.Filled.Star
    val OpenInNew: ImageVector = Icons.AutoMirrored.Filled.ExitToApp
    val Summarize: ImageVector = Icons.AutoMirrored.Filled.List
    val StickyNote2: ImageVector = Icons.Filled.Edit
    val Article: ImageVector = Icons.Filled.Description
    val CalendarToday: ImageVector = Icons.Filled.DateRange
    val TrendingFlat: ImageVector = Icons.AutoMirrored.Filled.ArrowForward

    // === NESTED OBJECTS FOR STYLE VARIANTS ===
    
    object Outlined {
        val Info: ImageVector = Icons.Outlined.Info
        val LocalFireDepartment: ImageVector = Icons.Outlined.LocalFireDepartment
        val Edit: ImageVector = Icons.Outlined.Edit
        val CheckCircle: ImageVector = Icons.Outlined.CheckCircle
        val Shield: ImageVector = Icons.Outlined.Shield
        val Warning: ImageVector = Icons.Outlined.Warning
        val Book: ImageVector = Icons.Outlined.Book
        val Send: ImageVector = Icons.AutoMirrored.Outlined.Send
        val FormatQuote: ImageVector = Icons.Outlined.FormatQuote
        val Psychology: ImageVector = Icons.Outlined.Psychology
        val Lightbulb: ImageVector = Icons.Outlined.Lightbulb
        val ArrowForward: ImageVector = Icons.AutoMirrored.Outlined.ArrowForward
        val Notifications: ImageVector = Icons.Outlined.Notifications
        val Search: ImageVector = Icons.Outlined.Search
        val OpenInNew: ImageVector = Icons.AutoMirrored.Outlined.ExitToApp
        val Refresh: ImageVector = Icons.Outlined.Refresh
        val SelfImprovement: ImageVector = Icons.Outlined.Face
        val School: ImageVector = Icons.Outlined.School
        val ChatBubble: ImageVector = Icons.Outlined.ChatBubble
        val TextFormat: ImageVector = Icons.AutoMirrored.Outlined.List
        val AutoAwesome: ImageVector = Icons.Outlined.AutoAwesome
        val BarChart: ImageVector = Icons.AutoMirrored.Outlined.TrendingUp // Fallback for Stats
        val Leaderboard: ImageVector = Icons.AutoMirrored.Outlined.List
        val Star: ImageVector = Icons.Outlined.Star
        val Email: ImageVector = Icons.Outlined.Email
        val Lock: ImageVector = Icons.Outlined.Lock
        val TrendingUp: ImageVector = Icons.AutoMirrored.Outlined.TrendingUp
        val MenuBook: ImageVector = Icons.AutoMirrored.Outlined.MenuBook
        val Image: ImageVector = Icons.Outlined.Image
        val Bolt: ImageVector = Icons.Outlined.Bolt
        val EmojiEvents: ImageVector = Icons.Outlined.Face
        val Handshake: ImageVector = Icons.Outlined.Face
        val Schedule: ImageVector = Icons.Outlined.DateRange
        val Flag: ImageVector = Icons.Outlined.Flag
        val Groups: ImageVector = Icons.Outlined.Person
        val MoreHoriz: ImageVector = Icons.Outlined.MoreVert
        val StickyNote2: ImageVector = Icons.Outlined.Edit
        val Article: ImageVector = Icons.Outlined.Face
        val TextFields: ImageVector = Icons.AutoMirrored.Outlined.List
        val CalendarToday: ImageVector = Icons.Outlined.DateRange
        val TrendingFlat: ImageVector = Icons.AutoMirrored.Outlined.ArrowForward
        val Tag: ImageVector = Icons.Outlined.Star
        val Circle: ImageVector = Icons.Outlined.Face
        val Summarize: ImageVector = Icons.Outlined.Face
        val Loop: ImageVector = Icons.Outlined.Refresh
        val CameraAlt: ImageVector = Icons.Outlined.Face
        val Stop: ImageVector = Icons.Outlined.Face
        val Mic: ImageVector = Icons.Outlined.Face
        val Pause: ImageVector = Icons.Outlined.Face
        val PlayArrow: ImageVector = Icons.Outlined.PlayArrow
        val SearchOff: ImageVector = Icons.Outlined.Search
        val AutoStories: ImageVector = Icons.AutoMirrored.Outlined.MenuBook
        val BookmarkBorder: ImageVector = Icons.Outlined.BookmarkBorder
        val HelpOutline: ImageVector = Icons.AutoMirrored.Outlined.Help
        val Tips: ImageVector = Icons.Outlined.Lightbulb
        val Quiz: ImageVector = Icons.AutoMirrored.Outlined.Help
        val OpenInFull: ImageVector = Icons.AutoMirrored.Outlined.ExitToApp
        val Settings: ImageVector = Icons.Outlined.Settings
        val CalendarMonth: ImageVector = Icons.Outlined.DateRange
        val NoteAlt: ImageVector = Icons.Outlined.Edit
        val NoteAdd: ImageVector = Icons.Outlined.Edit
        val BookmarkRemove: ImageVector = Icons.Outlined.BookmarkBorder
        val Diversity3: ImageVector = Icons.Outlined.Person
        val AlternateEmail: ImageVector = Icons.Outlined.Email
        val Contacts: ImageVector = Icons.Outlined.Person
        val Home: ImageVector = Icons.Outlined.Home
        val Person: ImageVector = Icons.Outlined.Person
    }

    object Rounded {
        val Check: ImageVector = Icons.Rounded.Check
        val Close: ImageVector = Icons.Rounded.Close
        val Info: ImageVector = Icons.Rounded.Info
        val Warning: ImageVector = Icons.Rounded.Warning
        val Person: ImageVector = Icons.Rounded.Person
        val Settings: ImageVector = Icons.Rounded.Settings
        val Star: ImageVector = Icons.Rounded.Star
        val Add: ImageVector = Icons.Rounded.Add
        val Edit: ImageVector = Icons.Rounded.Edit
        val Delete: ImageVector = Icons.Rounded.Delete
        val AutoAwesome: ImageVector = Icons.Rounded.Face
        val TrendingUp: ImageVector = Icons.AutoMirrored.Rounded.TrendingUp
        val Celebration: ImageVector = Icons.Rounded.Face
        val Favorite: ImageVector = Icons.Rounded.Favorite
        val Token: ImageVector = Icons.Rounded.Star // Fallback
        val Book: ImageVector = Icons.Rounded.Book
        val Send: ImageVector = Icons.AutoMirrored.Rounded.Send
        val Mail: ImageVector = Icons.Rounded.Email
        val EmojiEvents: ImageVector = Icons.Rounded.Face
        val Psychology: ImageVector = Icons.Rounded.Face
        val Stars: ImageVector = Icons.Rounded.Face
        val LocalFireDepartment: ImageVector = Icons.Rounded.Face
        val EditNote: ImageVector = Icons.Rounded.Edit
        val CalendarMonth: ImageVector = Icons.Rounded.DateRange
    }

    // === CUSTOM ICONS (Using standard icons for now to fix build) ===
    
    private val _customIcons = mutableMapOf<String, ImageVector>()

    // === SENTIMENT ICONS for Mood Selection ===
    val SentimentVeryDissatisfied: ImageVector get() = Icons.Filled.SentimentVeryDissatisfied
    val SentimentDissatisfied: ImageVector get() = Icons.Filled.SentimentDissatisfied
    val SentimentNeutral: ImageVector get() = Icons.Filled.SentimentNeutral
    val SentimentSatisfied: ImageVector get() = Icons.Filled.SentimentSatisfied
    val SentimentVerySatisfied: ImageVector get() = Icons.Filled.SentimentVerySatisfied
    
    val CheckCircle: ImageVector get() = Icons.Filled.CheckCircle
    val SelfImprovement: ImageVector get() = Icons.Filled.SelfImprovement
    val Lightbulb: ImageVector get() = Icons.Filled.Lightbulb
    val AutoAwesome: ImageVector get() = Icons.Filled.AutoAwesome
    
    // Explicit fills for compatibility or missing imports (Using safe fallbacks)
    val BookmarkBorder: ImageVector = Icons.Outlined.BookmarkBorder
    val SearchOff: ImageVector = Icons.Filled.Search
    val Clear: ImageVector = Icons.Filled.Close
    val Create: ImageVector = Icons.Filled.Edit
    val OpenInFull: ImageVector = Icons.AutoMirrored.Filled.ExitToApp // Fallback
    val NoteAlt: ImageVector = Icons.Filled.Edit
    val NoteAdd: ImageVector = Icons.Filled.Edit
    val BookmarkRemove: ImageVector = Icons.Filled.Bookmark
    val Diversity3: ImageVector = Icons.Filled.Person
    val Handshake: ImageVector = Icons.Filled.ThumbUp
    val Tips: ImageVector = Icons.Filled.Lightbulb

    // === COMMON ALIASES ===
    
    val Mail: ImageVector = Email
    val SendIcon: ImageVector = Send
    val ArrowRight: ImageVector = KeyboardArrowRight
    val ListIcon: ImageVector = List
    
    val Sms: ImageVector = Email
    val FilterList: ImageVector = List
    val Dashboard: ImageVector = Home // Fallback
    val Explore: ImageVector = Place
    val Forum: ImageVector = Email
    val Grass: ImageVector = LocalFlorist
    val Eco: ImageVector = LocalFlorist
    val Park: ImageVector = LocalFlorist
    val Nature: ImageVector = LocalFlorist
    val Shield: ImageVector = Info // Fallback
    val WorkspacePremium: ImageVector = EmojiEvents
    val MilitaryTech: ImageVector = EmojiEvents
    val Leaderboard: ImageVector = List // Fallback
    val ShowChart: ImageVector = TrendingUp
    val HistoryEdu: ImageVector = MenuBook
    val EditNote: ImageVector = Edit
    val TipsAndUpdates: ImageVector = Lightbulb
    val MarkEmailRead: ImageVector = Mail
    val MailOutline: ImageVector = Mail
    val ChatBubble: ImageVector = Chat
    val NotificationsActive: ImageVector = Notifications
    val Whatshot: ImageVector = LocalFireDepartment
    val RocketLaunch: ImageVector = LocalFireDepartment
    val Rocket: ImageVector = LocalFireDepartment
    val Fingerprint: ImageVector = Lock
    val Security: ImageVector = Lock
    val Policy: ImageVector = Shield
    val VerifiedUser: ImageVector = Verified
    val Groups: ImageVector = Person
    val Diversity: ImageVector = Person
    val Elderly: ImageVector = Person
    val Visibility: ImageVector = Info // Fallback
    val FitnessCenter: ImageVector = Info // Fallback
    val RecordVoiceOver: ImageVector = Mic
    val Hearing: ImageVector = Mic
    val Mood: ImageVector = EmojiEvents
    val EmojiNature: ImageVector = LocalFlorist
    val Terrain: ImageVector = LocalFlorist
    val Landscape: ImageVector = LocalFlorist
    val Water: ImageVector = LocalFlorist
    val Grain: ImageVector = LocalFlorist
    val Flare: ImageVector = WbSunny
    val LightMode: ImageVector = WbSunny
    val Nightlight: ImageVector = NightsStay
    val WbTwilight: ImageVector = NightsStay
    val BrightnessAuto: ImageVector = WbSunny
    val Science: ImageVector = Psychology
    val PsychologyAlt: ImageVector = Psychology
    val AutoGraph: ImageVector = TrendingUp
    val Map: ImageVector = Place
    val Storage: ImageVector = Dashboard
    val Memory: ImageVector = Dashboard
    val Inventory: ImageVector = Dashboard
    val CollectionsBookmark: ImageVector = Book
    val PhotoLibrary: ImageVector = Image
    val FilterFrames: ImageVector = Image
    val FilterAlt: ImageVector = FilterList
    val Tune: ImageVector = Settings
    val Help: ImageVector = Info
    val ExitToApp: ImageVector = Icons.AutoMirrored.Filled.ExitToApp
    val ArrowUpward: ImageVector = ExpandLess
    val ArrowDownward: ImageVector = ExpandMore
    val ArrowDropDown: ImageVector = ExpandMore
    val KeyboardArrowUp: ImageVector = ExpandLess
    val KeyboardArrowDown: ImageVector = ExpandMore
    val Remove: ImageVector = Close
    val ContentCopy: ImageVector = Edit
    val PlayCircle: ImageVector = PlayArrow
    val PhoneAndroid: ImageVector = Phone
    val Vibration: ImageVector = Phone
    val Today: ImageVector = CalendarMonth
    val Translate: ImageVector = MenuBook
    val VisibilityOff: ImageVector = Close
    val Moving: ImageVector = TrendingUp
    val NewReleases: ImageVector = AutoAwesome
    val CloudOff: ImageVector = Close
    val WifiOff: ImageVector = Close
    val Cloud: ImageVector = Info // Fallback
    val CloudDone: ImageVector = Cloud
    val CloudSync: ImageVector = Cloud
    val Cached: ImageVector = Refresh
    val Api: ImageVector = Code
    val BugReport: ImageVector = ErrorOutline
    val Construction: ImageVector = Settings
    val Handyman: ImageVector = Settings
    val Architecture: ImageVector = Settings
    val Foundation: ImageVector = Home
    val Badge: ImageVector = Verified
    val Diamond: ImageVector = Star
    val LocalOffer: ImageVector = Star
    val LockOpen: ImageVector = Lock // Fallback

    object Common {
        val Add = KairosIcons.Add
        val Check = KairosIcons.Check
        val Close = KairosIcons.Close
        val Home = KairosIcons.Home
        val Menu = KairosIcons.Menu
        val Person = KairosIcons.Person
        val Search = KairosIcons.Search
        val Settings = KairosIcons.Settings
        val Star = KairosIcons.Star
    }

    object Gamification {
        val Trophy: ImageVector = Icons.Filled.EmojiEvents
    }

    // === UTILITY METHODS ===

    private fun createIcon(name: String, pathData: ImageVector.Builder.() -> Unit): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply(pathData).build()
    }

    fun clearIconCache() { _customIcons.clear() }
    
    fun getIconMemoryInfo(): String = "Icons loaded"
    fun preloadCommonIcons() {}
}
