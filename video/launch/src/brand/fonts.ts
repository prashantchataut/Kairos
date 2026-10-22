// Local variable fonts, bundled with the film: the app's own three faces, copied from
// app/src/main/res/font (OFL licences in public/fonts). No network at render time.
// cinetic:brand-supplied serif, handwriting: Fraunces and Caveat are Kairos' faces (BRIEF.md).
import { staticFile } from 'remotion';

const faces: [family: string, file: string, weight: string][] = [
  ['Bricolage', 'fonts/bricolage.ttf', '200 800'],
  ['Fraunces', 'fonts/fraunces.ttf', '100 900'],
  ['Caveat', 'fonts/caveat.ttf', '400 700'],
];

if (typeof document !== 'undefined' && !document.getElementById('kairos-faces')) {
  const style = document.createElement('style');
  style.id = 'kairos-faces';
  style.textContent = faces
    .map(([family, file, weight]) => `@font-face{font-family:"${family}";src:url("${staticFile(file)}") format("truetype");font-weight:${weight};font-style:normal;font-display:block;}`)
    .join('\n');
  document.head.appendChild(style);
}
