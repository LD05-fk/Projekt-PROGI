# ArtConnect – tokeni dizajnskog sustava (nacrt)

Kontrasti izračunani po WCAG formuli (tekst ≥ 4.5:1).

## Boje

| Token | MUI putanja | Hex | Kontrast |
|---|---|---|---|
| Primarna | `palette.primary.main` | `#4338CA` | 7.9 s bijelim tekstom |
| Primarna tamna (hover) | `palette.primary.dark` | `#3730A3` | – |
| Primarna svijetla (pozadina chipa) | `palette.primary.light` | `#EEF2FF` | 10.2 s `#312E81` |
| Sekundarna | `palette.secondary.main` | `#BE185D` | 6.0 s bijelim |
| Pozadina | `palette.background.default` | `#FAFAF9` | 16.7 s tekstom |
| Površina (kartice, zaglavlje) | `palette.background.paper` | `#FFFFFF` | – |
| Tekst | `palette.text.primary` | `#1C1917` | 16.7 na pozadini |
| Tekst sekundarni | `palette.text.secondary` | `#57534E` | 7.3 na pozadini |
| Rub | `palette.divider` | `#D6D3D1` | dekorativno |
| Greška | `palette.error.main` | `#B91C1C` | 6.5 s bijelim |
| Upozorenje | `palette.warning.main` | `#B45309` | 5.0 s bijelim |
| Uspjeh | `palette.success.main` | `#15803D` | 5.0 s bijelim |
| Info | `palette.info.main` | `#1D4ED8` | – |
| Fokus prsten | (custom) | `#4338CA`, 3 px, offset 2 px | – |

## Tipografija

Font: **Inter** (Google Fonts, podržava č ć š ž đ), fallback `system-ui, sans-serif`.

| Varijanta | Veličina / visina retka | Težina |
|---|---|---|
| h1 | 36 / 44 px (xs: 28 / 36) | 700 |
| h2 | 30 / 38 px (xs: 24 / 32) | 700 |
| h3 | 24 / 32 px | 600 |
| h4 | 20 / 28 px | 600 |
| h5 | 18 / 26 px | 600 |
| h6 | 16 / 24 px | 600 |
| body1 | 16 / 24 px | 400 |
| body2 | 14 / 20 px | 400 |
| button | 14 / 20 px, bez uppercasea | 600 |
| caption | 12 / 16 px | 400 |

## Razmaci, oblici, sjene, točke prijeloma

- Razmak: `spacing = 8` (1 = 8 px, 2 = 16 px, 3 = 24 px …). Rubni razmak stranice: xs 16 px, sm 24 px, md 32 px.
- Radijusi: gumbi i polja 8 px, kartice 12 px, chip 16 px (pilula), dijalog 16 px.
- Sjene: kartica `0 1px 3px rgba(28,25,23,.08)`, kartica hover `0 4px 12px rgba(28,25,23,.12)`, dijalog `0 12px 32px rgba(28,25,23,.20)`.
- Prijelomne točke: xs 0, sm 600, md 900 (lg 1200, xl 1536 ostaju MUI zadane). Maksimalna širina sadržaja 1200 px.
- Minimalna ciljna površina dodira: 44 × 44 px.

## Chipovi statusa

| Status | Tekst | Pozadina | Tekst boja | Kontrast |
|---|---|---|---|---|
| `PREPARATION` | U pripremi | `#E7E5E4` | `#44403C` | 8.2 |
| `OPEN` | Otvorena | `#DCFCE7` | `#166534` | 6.5 |
| `CLOSED` | Zatvorena | `#FEE2E2` | `#991B1B` | 6.8 |
| `SELECTION` | Odabir u tijeku | `#FEF3C7` | `#92400E` | 6.4 |
| `FINISHED` | Završena | `#EEF2FF` | `#312E81` | 10.2 |
| `SUBMITTED` | Poslana | `#DBEAFE` | `#1E40AF` | 7.2 |
| `UNDER_REVIEW` | U razmatranju | `#FEF3C7` | `#92400E` | 6.4 |
| `NEEDS_INFO` | Potrebne informacije | `#FEF3C7` | `#92400E` | 6.4 |
| `ACCEPTED` | Prihvaćena | `#DCFCE7` | `#166534` | 6.5 |
| `REJECTED` | Odbijena | `#FEE2E2` | `#991B1B` | 6.8 |
| `WITHDRAWN` | Povučena | `#E7E5E4` | `#44403C` | 8.2 |

Status se ne smije prenositi samo bojom, uvijek ide i tekst.

## Navigacija po ulozi

| Uloga | Stavke |
|---|---|
| Posjetitelj | Prilike · gumb „Prijava s Googleom” |
| Umjetnik | Prilike · Portfolio · Moje prijave · korisnički izbornik (Profil, Odjava) |
| Organizator | Prilike · Moje prilike · Statistika · korisnički izbornik |
| Admin | Prilike · Administracija · korisnički izbornik |
