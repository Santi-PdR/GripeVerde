## Version 0.176.0 - Options and Quit step aside

In windows narrower than 620 pixels, Options and Quit used to hang from a fixed row at y=36, right on top of the title emblem, which starts at y=30. At 427x240, the size a 854x480 window gets at GUI scale 2, they covered thirty-two pixels of the crest. They now sit in their own rail to the right of Multiplayer and Mods, inside the same band as the button table, and the pair and the rail centre themselves on the emblem's axis. The emblem keeps its size and nothing new is drawn while you play.

## Version 0.175.0 - The emblem yields before the buttons

On short windows the title emblem now shrinks as a whole instead of growing down into the button table: at 427x240, the size a 854x480 window gets at GUI scale 2, the full block covered forty-five pixels of the buttons. The chronicler's note follows the same rule and steps down or waits when there is no room. The chain of the pocket watch starts below the shortcut icons, and clicking the emblem no longer triggers its seal over the top edge of a button. Nothing new is drawn while you play.

## Version 0.174.0 - The notices breathe again

The four rotating broadsides (rescue note, wanted poster, royal decree and the elixir label) now compose as one centred block. On tall posters the copy no longer clings to the upper edge, and on the shortest ones the steps tighten so the last line stops crowding the lower rule. On the elixir label the price now always keeps a line of air above the border. Nothing new is drawn while you play.

## Version 0.173.0 - The notice has a face

The quarantine notice on the title screen now opens with a larger engraved portrait of an infected man, framed like a printer's plate, with the blue eyes of the Ghouls and a caption that reads as period field copy. Below it, the order keeps its lines apart from the CEDA stamp, so nothing overlaps on short or narrow screens. The rotating CEDA field note follows the same plate, replaces its loose eye mark with a CEDA eye stamp, and hides the portrait when the poster is not tall enough. Everything stays still: nothing new is drawn while you play.

## Version 0.172.0 - A face for the quarantine notice

The title screen's quarantine order now carries a still, blue-eyed Ghoul portrait in its lower margin. Minecraft offers every regional locale translated by the mod, including Spanish (Uruguay), and the build checks all 25 entries.

## Version 0.171.0 - Room for the dispatch button

On compact title screens, the music control and shortcut icons now keep a clear gap. The CEDA Dispatch icon stays hidden until the row has enough width.

## Version 0.170.0 - The field stays open

Controls, Chat, Online and Language now preserve the live world behind their paper panels. A redundant black layer no longer hides the world when those screens open.

## Version 0.169.0 - Regional languages you can choose

The additional Spanish regions are now declared to Minecraft, so their language files can be selected from the language list as well as loaded when already selected. The build checks their language metadata along with all 25 translations and chronicles.

## Version 0.168.0 - English and Spanish, in every region

Every regional English and Spanish language file and field chronicle declared by the project is now bundled. The build checks all 25 locales for valid text, matching keys, and a current chronicle.

## Version 0.167.0 - More room in the rescue notice

The title-screen rescue notice now leaves a full line between instructions, including in the compact poster. The last sentence stays clear of the divider without adding motion or work during gameplay.

## Version 0.166.0 - A separate check for the street sounds

The ambience controls now play a brief creak through the same Minecraft channel as rain and wind. This checks the mod volume alongside Minecraft's Ambient and Master levels; the button is disabled during a world.

## Version 0.165.0 - The skyline remembers between frames

The attic window and its curtains now keep their saved state in memory for the session. Drawing the title screen no longer creates a position array or checks saved notes every frame.

## Version 0.164.0 - A welcome note that stays in its lane

The chronicler's welcome now fits between the side notices, wraps instead of stretching across them, and leaves the page after eight seconds.

## Version 0.161.0 - Clearer button subtitles

The second line on the main buttons keeps stronger contrast against the dark panels, including on hover, while staying within the aged-brass palette.

## Version 0.160.0 - Clearer settings

Settings categories now use familiar names in English and Spanish. Sound tips say plainly which Minecraft volumes to check, and the rescue note clearly explains what happens to a fallen teammate's inventory and body.

## Version 0.159.0 - Clear menu lettering

Diary labels now render as plain Minecraft text, without inherited font styles. The build check guards this path against custom font styles returning.

## Version 0.158.0 - Spanish notes, properly written

The Spanish captions for the shooting star and sealed telegram now include their missing accents and ñ. English wording is unchanged.

## Version 0.157.0 - A quieter telegram

The sealed note is narrower and leaves the title screen sooner. The secret remains, but the sky and the printed notices are easier to see.

## Version 0.156.0 - A lighter note from the sky

The shooting-star caption keeps its wrapped lines while the message, font, and available width stay the same. It looks unchanged without splitting and measuring the same text on every frame.

## Version 0.155.0 - Clear letters again

The custom font had left empty boxes across the title screen. The diary now uses Minecraft's regular lettering, and the little eye's clue reads like a note someone found rather than a menu instruction.

## Version 0.154.0 - Notes from the living

The field guide now reads like a survivor's account instead of a system briefing. Its English and Spanish notes explain how Ghouls change, what to do when a teammate is pinned or downed, and what each difficulty brings, while preserving the server's mechanics.

## Version 0.153.0 - Clearer letters in the diary

The title screen, options, field guide, and shared controls now use Atkinson Hyperlegible with consistent text measurements. Button labels no longer draw a second dark copy, so they stay crisp against the paper. The font is bundled with its license and adds no animation or rendering work in worlds or inventories.

## Version 0.152.0 - Ink that stays put

The menu buttons' tiny paper-wear marks are laid out once and reused on every draw. The paper keeps its same look without reseeding and sampling a random generator every frame.

## Version 0.151.0 - A signal without blinking

When Reduced Motion is on, the attic signal window stays steady instead of flashing its answer. The clue remains available, and the menu atmosphere stays still.

# Eternal Craft: Gripe Verde

## Version 0.150.0 - The field guide fits the page

The field guide now fits short windows and high GUI scales, wraps its notes to the paper width, and keeps the return button visible. In compact layouts, the reading hint moves into the heading instead of crowding the page.

## Version 0.149.0 - Columns that hold their place

The title-screen column layout and left notice bounds are reused until the window size changes. Redraws no longer recreate those layout regions each frame, while paper placement, resize behavior, and poster interaction remain the same.

## Version 0.148.0 - Still paper, lighter frames

The title-screen notices now reuse their translated copy keys and paper-cut details instead of building temporary arrays on every draw. Seeded blood flecks use allocation-free arithmetic in place of a fresh random generator each frame. The marks remain fixed to the menu and never render over the world or inventory.

## Version 0.147.0 - CEDA dispatch at hand

A small CEDA note on the title screen opens the optional CEDA Dispatch settings when that mod is installed. Both screens now share quiet parchment and sepia styling. Ink stamps stay in the lower paper margin and skip short sheets; their fixed design avoids a random allocation each menu frame. The shortcut adds no gameplay or inventory effects.

## Version 0.146.0 - Room for the reward

The wanted poster now keeps its portrait and wax seal clear of the reward lines on shorter windows. The drawing stays fixed to the title screen.


## Version 0.145.0 - The right address keeps its place

When both an official Outbreak address and a renamed saved entry exist, the multiplayer page now pins the matching address first. If only the renamed entry remains, its custom address and label stay intact.

## Version 0.144.0 - The cry that brought us back

Reaching the teamwork chapter can leave one last recollection from the chronicler: a cry carried across the town, and strangers running back for one another. It appears once, in the chosen language.

## Version 0.143.0 - The fever still had a pulse

A witness's recollection now accompanies the infection chapter. It tells of the moment the survivors understood that the fever had not killed its victims. The note appears once, in English or Spanish.

## Version 0.142.0 - The note waits at the right page

The rescue recollection now appears whichever way the reader reaches that section: by turning to its mark, scrolling through the pages, or using the keyboard. It remains a one-time note.

## Version 0.141.0 - The way back through the mud

Reading the rescue protocol leaves a new note from the chronicler. It is a one-time recollection of a frightened group turning back for one of their own, written in the chosen language and shown on a later page.

## Version 0.140.0 - The address through the fog

The official Outbreak server is now recognized even if its saved address uses different capitalization or has stray spaces. Older addresses are migrated the same way, so the field guide won't add a second copy of the server.

## Version 0.139.0 - The Outbreak keeps its name

The pinned Ghoul Outbreak server now appears in the selected language. Returning players keep their own server name, while an old saved Outbreak address is updated even if it was renamed.

## Version 0.138.0 - The pen waits for the reader

The chronicle now writes only while its next line is visible. Scrolling away pauses the ink, long frame delays cannot make it jump ahead, and the quiet scratch follows visible writing less often.

## Version 0.137.0 - The doctor's mark finds its place

The elixir's fraud mark now sits in a dedicated space below the bottle copy and disappears when a short poster has no room for it. English readers see “FRAUD”; Spanish readers see “FRAUDE.”

## Version 0.136.0 - A light crossing the language barrier

The shooting-star note now follows the chosen English or Spanish language while it is visible and wraps inside the screen on small windows.

## Version 0.135.0 - A note that crosses the language barrier

The hidden message behind the ministry seal now changes with the selected English or Spanish language, even while the note is still on screen.

## Version 0.134.0 - The chronicler keeps your tongue

The title greeting, paper label, and quiet vigil line now follow the selected English or Spanish language. Their wrapped text is reused until the words, window width, or font changes.

## Version 0.131.0 - Ambient controls reach every layer

Menu ambience one-shots now use Minecraft's Ambient sound category, matching the looping beds and the Ambient slider. Music remains in Music, and button effects remain in the UI/Effects path.

## Version 0.132.0 - The gramophone keeps its note

The title-screen music button now updates its tooltip only when playback changes or the language changes, rather than rebuilding it every tick.

## Version 0.133.0 - The notice keeps its lines

The title-screen typewriter notice now reuses its wrapped lines and cursor width until the visible text, window size, or font changes, avoiding repeated layout work between frames.


## Version 0.130.0 - A note in the margins

Rewrite the smuggler's hint in the chronicler's natural voice in English and Spanish, removing control-like wording while keeping its in-world clue.


## Version 0.129.0 - Words from the watch

Polish the English and Spanish title-screen lines so they sound natural aloud, keep the period tone, and use correct Spanish accents. The button subtitles remain static text and still rotate only in the title screen.


## Version 0.128.0 - Sickly glow on the horizon

The title-screen city gains a faint, fixed green haze behind the skyline, hinting at the outbreak. It is menu-only, uses a few translucent fills, and adds no animation or world rendering.


## Version 0.127.0 - Worn paper, clear notice

The elixir notice now keeps its fraud stamp below the copy, with a safe margin from the price and bottle line. Notice sheets gain subtle paper depth, a fine inner rule, and uneven cut edges. All details stay still and only render on menu papers.


## Version 0.126.0 - Words stay on the paper

Translated lines on the quarantine, elixir, reward, and royal decree notices now fit inside the paper edge. Scaled headings respect the same width, keeping the English and Spanish copy clear of the border and folded corner.

## Version 0.125.0 - The empty desk

The long-idle discovery now speaks of an empty desk and the ghosts who gather there instead of naming the controls used to uncover it. Its secret and timing are unchanged.

## Version 0.124.0 - A note from the night watch

The title greetings now sound like the chronicler speaking to someone beside him. They use short, everyday English and keep their place in the Ghoul-era story.

## Version 0.123.0 - Clouds without frame waste

The title sky now reuses its fixed cloud layout instead of rebuilding it on every frame. Their movement and appearance stay the same, with fewer short-lived allocations while the menu is open.

## Version 0.122.0 - A stamp with room to breathe

The elixir's FRAUDE mark now sits after the bottle line instead of being forced up from the card's bottom edge. It cannot cover the words on a short sheet, and stays hidden if the notice has no room for it. The title notices also have more visible hand-cut corners, with the ink border following the paper edge. Everything remains still on the title screen.

## Version 0.121.0 - Sections you can spot

Each field-manual heading now has a small printed number that matches the 1–7 shortcuts in the footer. Readers can see which section a shortcut opens without counting headings. The marks are static and remain confined to the manual.

## Version 0.120.0 - Rescue advice that fits

The rescue notice uses shorter lines so every instruction stays readable on the narrow card: a teammate frees a PIN, a lift takes five seconds, the helper must avoid hits, and a defibrillator comes from loot.

## Version 0.119.0 - No one left behind

A fifth daily CEDA notice gives the rescue basics: a teammate must free anyone caught in a PIN, hold to lift a fallen survivor for five seconds without taking a hit, and find a defibrillator in the loot to revive the dead. The paper is static and title-screen-only.

## Version 0.118.0 - Pages with a hand-worn edge

The notices now have gently clipped corners, a small fold, and a few worn paper fibres. Their details stay still, including while you play. The elixir's red FRAUDE stamp has its own space below the price, and the quarantine notice still opens the field guide when pressed. The chronicler's notes now sound more like a person telling what happened, in English and Spanish.

## Version 0.117.0 - Lighter return dust

Cache the title-screen dust pattern shown after a long absence. Frames reuse the same 40 specks and opacity values; only a window resize recalculates them. The effect remains static and title-screen-only.

## Version 0.116.0 - Keyboard-ready multiplayer hints

Full action tooltips now appear after the usual short delay when a custom multiplayer button receives keyboard focus, as well as on hover. Focused hints are placed beside their button. Gameplay screens remain still.


## Version 0.115.0 - Full multiplayer action hints

The custom multiplayer buttons now show their full localized tooltips after Minecraft's usual short hover delay. Compact buttons stay compact, while the server list, its layout, and live gameplay remain unchanged.

## Version 0.114.0 - Lighter server list rendering

Read the vanilla server-list row dimensions when the list is laid out, then reuse them while rendering its paper cards. This avoids repeated private-field lookups per frame without changing the official address, saved servers, ping display, or LAN scan.

## Version 0.113.0 - A book instead of a letter

Replace the single “F” with a small open-ledger icon so the field-guide shortcut reads the same in English and Spanish. Its full localized name remains available to keyboard and screen-reader users.

## Version 0.112.0 - Clean field guide headings

Section names remain printed on the paper instead of being covered by tiny button panels. Navigation targets stay inside the reading viewport, preserving keyboard and screen-reader access without hover sound or gameplay animation.

## Version 0.111.0 - Field guide at a glance

Open the bilingual CEDA field guide directly from the title screen with a small, labeled button beside the chronicle control. It uses the existing localized hint and adds no world, inventory, or gameplay animation.

## Version 0.110.0 - Clearer button ink

Disabled button labels use darker sepia ink for clearer reading against the paper. Button text keeps one unshadowed draw pass and avoids duplicate clipping work.

## Version 0.109.0 - Notes worth reading

Chronicler notes now wrap onto a compact paper card instead of losing the end of longer lines. The restrained CEDA seal stays static, and archive notes remain queued when a short viewport cannot show them clearly.

## Version 0.108.0 - Music on command

Pause or resume the menu soundtrack from the sound panel. The choice is saved and does not alter the volume setting. Track controls stay unavailable during a live world.

## Version 0.107.0 - Eyes in quarantine

Add a small, fixed pair of blue Ghoul eyes to a distant upper window on the title screen. Fog softens the detail; diary overlays and live worlds do not render it. The scene uses static pixels only, with no extra ticks, particles, sounds, or gameplay effects.

## Version 0.106.0 - Steadier paper

Cache each panel's deterministic moisture pattern. The paper keeps the same appearance without creating a random generator on every render. Draw the Ghoul report's three lines directly instead of allocating a temporary array each frame. Gameplay behavior is unchanged.

## Version 0.105.0 - Quiet notes

Respect Reduced motion in the chronicler's marginal notes, discovery seals, and the sound-panel tuning fork. Notes appear at once and the seal stays steady when the preference is on. With the preference off, the existing menu behavior remains; interfaces during live sessions stay still.

## Version 0.104.0 - The field report

Add a fourth daily title-screen document with CEDA guidance on living Ghouls, mutation behavior, PIN rescues and staying together. Give it a worn-paper treatment and a fixed blue-eye ink mark. The poster is bilingual and static, with no animation in worlds, servers or inventory screens.

## Version 0.103.0 - CEDA's cover story

Clarify that CEDA concealed the 1800 outbreak as common flu and that Ghouls are living infected survivors, not raised dead. Keep the field-guide layout unchanged.

## Version 0.102.0 - The royal decree

Add a small crown-in-wax seal to the rotating royal decree. Keep it inside the paper border and use static shapes on the title screen only.

## Version 0.101.0 - Clearer sound guidance

Clarify that Minecraft Master and the matching category both multiply mod music and ambience. Keep the bilingual slider help concise and make the controls to check explicit. Playback and world behavior are unchanged.

## Version 0.100.0 - Compact sound readout

Keep Minecraft Master, Music, and Ambient percentages visible on narrow sound screens with concise labels. Retain the complete category names in the regular layout.

## Version 0.99.0 - Master volume visibility

Display Minecraft's Master level beside Music and Ambient in the sound cabinet, so a muted master volume is visible while checking menu audio. The readout only observes settings and does not change playback or world behavior.

## Version 0.98.0 - First-response signals

Use restrained color rails to make PIN, downed, death, and defibrillator instructions easier to find. Preserve the guide's regular-weight text, keyboard controls, and scrolling. The callouts are static and add no world effects.

## Version 0.97.0 - The chronicler's field note

Show title greetings on a fuller archive-paper slip with a catalog heading in both languages. Improve spacing and ink hierarchy while keeping the text regular-weight and easy to read. Keep the paper detail static and confined to the title screen.

## Version 0.96.0 - Minecraft sound levels

Show the Music and Ambient category volumes from Minecraft beside the mod's own levels, making a muted game category easier to spot.

## Version 0.95.0 - Live sound levels

The sound cabinet now displays the current music, ambience, and interface-effect levels while you adjust them, using the translated labels.

## Version 0.94.0 - The chronicler speaks

Show session greetings on the title screen as quiet, static paper notes for twelve seconds. Keep them out of compact layouts, fit them to the paper slip, and prevent long vigil lines from overflowing narrow windows.

## Version 0.93.0 - Find the fallen survivor

Add the body's death-site location to both field-guide recovery notes so teammates know where to search. Keep the instructions mirrored in English and Spanish.

## Version 0.92.0 - Polished Spanish archive copy

Restore accents and opening punctuation in the Spanish telegrams and hidden archive lines. Clarify the contraband note while preserving every discovery condition.

## Version 0.91.0 - Readable archive notes

Show the chronicler's archive note in a compact horizontal paper slip above the records at regular window sizes, keeping the margin fallback for short screens. Restore accents in the Spanish archive status copy.

## Version 0.90.0 - Localized title greetings

Move chronicler greetings into the English and Spanish language resources. Correct Spanish accents and keep the vigil notes in direct address.

## Version 0.89.0 - Legible Ghoul headings

Use dark teal ink for field-guide section headings on parchment, including the hovered and keyboard-focused states. Keep the Ghoul eye glow as decoration and preserve the regular type weight.

## Version 0.88.0 - Readable archival ink

Darken the regular-weight flavor ink used for secondary copy and remove an extra fade from the corrected options note. Hierarchy stays in spacing and color instead of heavier font weight.

## Version 0.87.0 - Balanced paper grain

Reduce repeated grain draw calls on full-size menu papers while keeping their fibres visible as slightly longer, fixed strokes. The live-world paper path remains the same.

## Version 0.86.0 - Compact discovery notices

Shorten archive discovery notices into a smaller bilingual paper slip with a narrow width cap and a safer text margin. Keep notices off the title screen and suppress them while a world is active.

## Version 0.85.0 - Weathered recovered papers

Recovered documents in the archive gain a folded corner, softened shadow, and small fixed wear marks. Their labels keep a clear margin and the details render only inside the archive.

## Version 0.84.0 - A clearer soundtrack console

The sound cabinet now shows which menu record is playing, gives a compact way to move to the next record, and reports when a transition or pause is underway.

## Version 0.83.0 - Polished Spanish copy

Correct missing accents in Spanish menu and field-guide text, use consistent direct address, and make the sound controls clearer in Spanish. English strings and localization placeholders remain unchanged.

## Version 0.82.0 - Readable chronicler notes

Horizontal notes now stay within the window and sit on a small paper strip for contrast. Spanish notes also regain their accents.

## Version 0.81.0 - Aged field paper

Give fixed in-world paper panels worn corners and a quiet fold highlight. The effect uses a few static fills, with no animation or tick work during play.

## Version 0.80.0 - Discovery notices that fit

Discovery seals now fit narrow windows and shorten long names to stay inside the screen.

## Version 0.79.0 - Explain the sound controls

The music and ambience sliders now explain which Minecraft sound category also needs to be audible.

## Version 0.78.0 - A clearer field guide

The survivor manual now gives body entries breathing room and uses a slightly taller line rhythm for easier reading.

## Version 0.77.0 - Keep your server address

The multiplayer screen now keeps an address you edit for the pinned Ghoul Outbreak entry. Only the previous built-in host is migrated to the current address.

## Version 0.76.0 - Still controls during play

Mod buttons, toggles, and alert accents now switch immediately instead of animating whenever a world is open. The title menu keeps its atmospheric motion outside gameplay.

## Version 0.75.0 - A signature that fits

On narrow title layouts, only the current signature appears, clipped to the space between the music control and the right edge. Wide layouts keep the crossed-out previous signature.

## Version 0.74.0 - Cleaner field-guide headings

The field guide now draws its title and subtitle with the diary's regular centered text style, without Minecraft's automatic dark drop shadow.

## Version 0.73.0 - Notes away from the main desk

Contextual notes no longer appear over the title controls. They remain queued and appear inside paper screens when opened.

## Version 0.72.0 - Ambience control restored

Sound settings now include an on/off control beside the ambience slider. Its current state and tooltip are available in English and Spanish.

## Version 0.71.0 - A quieter title screen

Discovery cards no longer overlay the title screen. Discoveries remain recorded in the archive, and contextual marginal notes are unchanged.

## Version 0.70.0 - A title menu that fits

Narrow windows now reflow the title actions to avoid overlap. Side posters and the pocket watch step aside in compact layouts, while options, quit, and music controls stay reachable. This change only affects the title screen and adds no in-world animation or tick work.

## Version 0.69.0 - Audio sliders without repeated saves

Music and ambience volumes continue changing live while dragging, but each setting is saved once when the slider is released.

## Version 0.68.0 - One-shot menu audio shutdown

Menu music and ambience now stop once when entering a world instead of being asked to stop on every client tick. The guard resets when returning to the title menu.

## Version 0.67.0 - Safer withdrawal from Outbreak

Leaving a remote session now asks for confirmation. The Ctrl+0 shortcut only works when the optional debug shortcut is enabled.

## Version 0.66.0 - Outbreak survival briefing

The field guide now warns against exploring alone, explains that actions shown in chat can have requirements, and clarifies that high difficulty ranks increase general infected stats and add global events.

## Version 0.65.0 - Complete reduced-motion feedback

- Lantern hover feedback is immediate when reduced motion is enabled.
- Reuse the animated-window lookup during each render without changing the scene's appearance.

## Version 0.64.0 - Menu audio stops in-world

Menu music and ambience now stop as soon as a world becomes active, even when the menu screen no longer ticks during connection or gameplay.

## Version 0.63.0 - Consistent fallback language

With only English and Spanish resources included, auxiliary UI now uses Spanish only for es_es and English for unsupported Minecraft locales.

## Version 0.62.0 - Official server ready to join

The pinned Ghoul Outbreak server is selected when Multiplayer opens without an existing selection. The player remains in control and must press Join to connect.

## Version 0.61.0 - Lighter server-list refresh

The multiplayer screen no longer reads the server file immediately before opening a replacement list. Vanilla loads the saved list once during screen initialization, avoiding a duplicate synchronous disk read after add, edit, delete, or refresh actions.

## Version 0.60.0 - Consistent reduced motion

The reduced-motion option now stops UI hover easing, press movement, decorative pulses, the pocket-watch swing and glass sweep, and typewriter reveal. Gameplay screens remain static while a world is active.

## Version 0.59.0 - Official server within reach

The pinned Ghoul Outbreak server now stays in the first multiplayer row, updates its address in place, and refreshes the visible list without creating duplicates.

## Version 0.58.0 - Quiet lore during live play

Discovery toasts and marginal notes wait while a world is active. Pending notes stay queued instead of being read, saved, or played from a pause screen, keeping those screens static and avoiding file work during play.

## Version 0.57.0 - Audio mix status

The sound cabinet subtitle now shows the current music, ambience and interface SFX percentages, even on compact screens. Title tracks now use Minecraft's Music category, and the preview tooltip explains which controls affect the test sound.

## Version 0.56.0 - Compact sound cabinet

The sound cabinet now keeps its options, three volume controls, preview, and return button inside short viewports. In a live world it uses a fixed veil and does not trigger the title-menu silent-record discovery.

## Version 0.55.0 - Spanish copy pass

Spanish menu text now has corrected accent marks and consistent direct address. The English and Spanish key sets and screen behavior remain unchanged.

## Version 0.54.0 - Sound check guidance

The sound preview tooltip now explains that it uses the current interface SFX level and that Minecraft's master volume still controls audibility.

## Version 0.53.0 - Clear field manual

The Spanish field guide now uses consistent direct address, corrected accents, and clearer instructions for reviving teammates. No gameplay behavior or active-world animation changed.

## Version 0.52.0 - Field manual from pause

The pause menu now opens the field guide through a secondary action. Compact layouts show a shorter label while keeping the full tooltip, and the guide returns to pause. Its in-world paper overlay remains static.

## Version 0.51.0 - Field rescue protocol

The field guide now includes a fast rescue sequence: respond to the global trapped alert, free a PIN, revive a downed teammate, and use a looted defibrillator if they die. All seven sections can be opened with number keys.

## Version 0.50.0 - Localized headers fit every panel

Localized titles, subtitles, and flavor lines now fit within the parchment panels across the remaining settings, archive, and confirmation screens. Confirmation messages also wrap to the actual panel width at compact resolutions, keeping text readable without changing navigation.

## Version 0.49.0 - Compact multiplayer actions

The multiplayer actions now fit within the available panel at compact resolutions. Short English and Spanish labels appear when needed, and tooltips retain each full action name. The heading and subtitle stay within the parchment.

## Version 0.48.0 - Server forms that fit

Direct connect and server editing now derive their panel, input, and button sizes from the visible panel. The controls stay inside the parchment, headings and field labels stay within its width, and the full action labels remain available in tooltips. The hidden-address warning now appears inside the direct-connect panel instead of below the edge of a short window.

## Version 0.47.0 - Compact survivor pause screen

The pause cabinet now switches to a two-column action layout when the viewport is short, with the leave action spanning the final row. The header and dividers move with the compact layout, long subtitles stay inside the paper, and hover tooltips preserve complete English and Spanish control names. Decorative details step aside when the compact paper needs the room. The in-world panel remains static and keeps the hotbar clear.

## Version 0.46.0 - Responsive options cabinet

The options cabinet now fits compact windows without letting the two-column grid, FOV slider, or Done button spill beyond the paper. In compact layouts, controls and spacing shrink together and decorative flavor copy steps aside. Long English and Spanish labels stay available in hover tooltips, and the title and subtitle respect the panel width.

## Version 0.45.0 - Interface sound controls

The sound cabinet now has a persistent interface SFX slider and a preview button. It controls menu clicks, hovers, scrolling, notifications and custom UI sounds, while Minecraft's own sound settings remain in effect.

## Version 0.44.0 - Clearer survivor screens

The title screen no longer shows the rotating lower-right quarantine report. The pinned server is now Outbreak.exaroton.me:24652. Button text is drawn once for a cleaner weight, UI sounds are easier to hear, and archival papers gain subtle static folds. Diary transitions are skipped for the inventory and throughout a live world. Only US English and Spain Spanish remain as packaged locales.

## Chronicle under seal

## Version 0.43.0 - Still effects during a live session

The diary no longer eases scrolling or animates flashes, the mirror, eye or tuning fork while a world is open. Navigation responds immediately.

## Version 0.42.0 - Let the live world breathe

In a world, the diary now uses a fixed veil and panel. It pauses the animated sky, storm, grain, particles, decorative effects, page transitions and eased button hover while keeping options readable over the world. The title menu keeps its atmosphere.

## Version 0.41.0 - Difficulty tiers, unsealed

The field guide now lists the live difficulty ladder through INSURGENCY and INFERNUM, plus the BACKBREAKING, DOOMSDAY, and ARMAGEDDON ranges marked WIP. It explains the added global events and stronger ghouls.

## Version 0.40.0 - The bookmark follows the chapter

The field guide now remembers your place within its chapter, so changing the window size or GUI scale will not send you to a different passage. Dragging the scrollbar also keeps narrated chapter controls aligned.

## Version 0.39.0 - The bookmark survives a restart

The field guide now remembers your place after you close the game. Open it again and continue with the same passage.

## Version 0.38.0 - The field guide remembers

Reopen the CEDA field guide during the same game session and it returns to the passage you were reading. Scrolling, chapter jumps, and dragging the scrollbar all keep the bookmark in sync.

## Version 0.37.0 - The manual can be heard

The field manual now works with Minecraft's narrator: Tab visits its six chapters and each stop reads the heading and its protocols. Enter jumps to the selected chapter, with a visible blue focus mark on the paper. The footer also lists these controls.

## Version 0.36.0 - The origin of the fever

The infection file now records how local habits, genetics, and health shape mutations, and reveals the 1800 cover-up. Authorities called the outbreak a common flu; Ghouls remain alive, and the Green Flu does not raise corpses.

## Version 0.35.0 - Chapter jumps

The manual now jumps straight to any of its six chapters: click a heading or press 1–6. Hovering headings glow and underline to show they are interactive, and the footer includes the new navigation shortcut.

## Version 0.34.0 - The survivor lineages

The field manual now records the server's survivor races: more than 300 possibilities, including Humans, Sharks, and Cyborgs, with V1–V4+ evolutions earned through rarity-specific actions and missions. It also lists the full rarity scale shared by races, artifacts, and items.



## Version 0.33.0 - Manual navigation

The field manual now scrolls with the wheel, arrow keys, and Page Up/Page Down; Home and End jump to either edge. Its scrollbar can also be dragged, and the footer shows the controls. All three navigation methods stay within the paper and keep the return-to-safe-room button available.

## Version 0.32.0 - The field manual

The uncensored CEDA field copy opens from the quarantine poster. It gathers the server's real protocols: ghouls remain alive, PIN attacks require a rescue, downed survivors can be revived, death requires a defibrillator, supplies dwindle, and difficulty tiers escalate. The manual scrolls with the wheel, returns to the poster, follows the game language, and uses ice-blue details for the infected eyes.

## Version 0.31.0 - Quarantine dispatches

The title now posts a different quarantine dispatch each day: CEDA insists it is only a common flu, while blue eyes and mutations tell another story. The notices also warn that supplies dwindle, a teammate must free the pinned survivor, and the group should regroup. In October, the chronicler stops calling the ghouls dead: they are still alive. The dispatch keeps clear of the signature and hides when the window has no room.

## Version 0.30.0 - The whole leaf

Corrections from the study's elder, carefully noted. The nib no longer writes a single entry while the others stand printed: the whole ledger now writes itself before the reader as they move down its leaves — what was left far above counts as printed, and jumping down never leaves gaps; a single touch finishes it all. The magnifier also learned to be read: its little box is now a label of clear paper with ink lettering, not a dark stain where the word got lost. The buttons' ticks as the hand passes over them lowered their voice. And three oversights of the press are amended: the signature line no longer nears the desk of buttons, the day's notice reads at its left on the same line, and the quotes of the cabinet and the sound room never again end up under buttons or outside the paper.

## Version 0.29.0 - The way back and the magnifier

The way back turned courteous: returning from a desk to the one before no longer calls the full ink ceremony, only a brief gesture — the leaf falls from above, settles, and that is all; the long sweep, the sprinkles and the drops are now reserved for going forward. And the ledger gains a magnifier: at the foot of the page, to the right, there is a little box where the reader writes a word, and the chronicle carries them from one sighting to the next, marking every line where the word dwells; a gesture closes it and the ledger forgets the question. Also, the desk no longer answers with a dull knock when touched in silence: the owner of this study asked for silence and silence there shall be. And the page-turning had its screws tightened: when there is no ink in the air, the cabinet spends not an instant pretending there is.

## Version 0.28.0 - The drops and the folios

The ink the curtain drags no longer passes dry: drops break off its edge, slide a short span and leave their fine trail, like a laden quill moved too fast; those who ask for reduced motion never see them. And the chronicle gains a folio of its own: at the foot of the page, centered, a discreet mark tells which leaf the reader is on and how many the ledger holds, and it advances on its own with the reading. Also, if the cabinet advanced more than one edition since the last visit, every entry the reader skipped wakes stamped in wax as freshly arrived — not just the latest; and the title board learned a few new lines for its rotating notices.

## Version 0.27.0 - The fold and the pen

The leaf has learned where the reader comes from: advancing to a new desk sweeps the ink downward to uncover it, and returning to a desk already visited sweeps the ink upward, like turning a page of the tome back; the curtain's edge sprinkles tiny drops as it passes and, once uncovered, the new leaf settles onto the desk with a brief shadow. And the chronicle has learned a trade: the entry of the freshly arrived edition writes itself before the reader, letter by letter, the nib resting on the last stroke with a quiet scratch across the paper; the impatient finish it with a single touch, and those who ask for reduced motion receive it whole, already written.

## Version 0.26.0 - Leaves and lanterns

Moving from one cabinet screen to another — title, options, chronicle, pause — the new leaf is revealed beneath an ink curtain that sweeps from top to bottom, like a page turned with a dry hand; those who ask for reduced motion receive only a brief fade, no sweep, and screens outside the cabinet receive no adornment at all. And the avenue lanterns have learned to know the reader's hand: bring it slowly near one of them and its halo breathes wider and its flame rises a little, never to go out. And the cabinet tuned its ear as well: the Chronicle, the lodgings ledger and the Sound Cabinet now each open with a touch of their own, as was always intended; rapping the desk when the air has gone silent answers with a dull knock, barely audible; the gramophone heralds each new piece with a gesture of its own; and coming back to the vigil from any room seats the reader once more, like a door closing properly at one's back. The chronicles of recent editions have also been rewritten with the pen of a real-life document: no workshop words left, only craft and paper.

## Version 0.25.2 - The telegram never touches the desk

A reader with a large interface scale (3 or 4) reported two desk accidents: the secrets telegram fell over the button desk and became unreadable, and the elixir's FRAUDE stamp climbed over the poster's lettering. The chronicler reorders his bounds: the telegram now refuses, above all, to touch the desk — if the free band shrinks, it rises above the logo strip and never leaves the window — and its width is trimmed on narrow screens; and the red stamp is always placed after the text and inside the paper, at any scale. No new secrets: just a tidy desk.

## Version 0.25.1 - The original cylinder

The new rain sounded like machine static, and the chronicler owns the mistake: the cylinder had been re-recorded instead of cleaned. The synthesis is withdrawn and the original recording of rain on cobblestone returns — the same as ever, drops and all — now carefully amplified without crushing its peaks; the street wind, too, returns to its original master, simply raised in volume. To give the rain back its place in the mix, its inner dial steps up. Both loop seams were measured: they joined clean, so they carry no crossfade.

## Version 0.25.0 - The storm watcher

Three new secrets fit in one hand. On nights when the sky breaks, whoever stays through three flashes before the vigil without leaving earns a card of their own: the chronicler counts the lightning for you. The rain, too, has fallen close enough to touch: a well-aimed drop lets itself be caught with a click, sounds like a needle on the cylinder, and climbs back to the sky (it never rains over the buttons). And in the hidden telegraph, typing DOCTOR brings a whisper and a low bell: the elixir had a doctor, the practice still stands, and his signature keeps arriving on forms nobody sends. The archive grows to fifty-two cards.

## Version 0.24.1 - The chronicler's errata

A single crooked letter on the freshly printed front page halted the press: the raindrop seed had been written with a character that does not exist in the hexadecimal alphabet, and edition 0.24.0 never compiled in the workshop (the fault surfaced on the very first deployment attempt). The errata is now set in valid ink — the drops fall exactly as before, just from another seed — and the cabinet gains one more printing seal: a mechanical proofreader now inspects every numeric literal in the codex before any edition leaves stamped. Everything announced in 0.24.0 remains inside, intact.

## Version 0.24.0 - The wind blows again

For weeks the street held its breath: the wind around the cabinet had been recorded so faint that no gust ever crossed the window, and with it the rain, the footsteps and the whispers barely coughed. The chronicler inspected the cylinders and restored every layer to its proper volume; the ambience dial finally governs something. With open ears come storm nights, the chronicle now opens at the freshly arrived entry and stamps it NEW until you read it, rain slides down the parchments, the telegram's wax yields to a stubborn hand, and a hidden tuning fork brings the whole room into pitch. The street, at last, stops playing once the reader crosses into a world.

## Version 0.23.2 - The telegram arrives intact

One of the cabinet's telegrams used to shatter the whole desk by arriving empty: clicking the title's seal could make the typewriter open its envelope before a single letter had landed, and reading that letterless envelope halted the client outright (the fault arrived in a player's field report on the 0.22.0 edition). The messenger now waits for at least one letter before counting lines, and that purely decorative reading is now fenced in: no ornamental fault will ever bring the vigil down again.


## Version 0.23.1 - Seals and clockwork

No new scenery this round: the build gains two more seals. One guards that every ambient subtitle the city announces exists in all twenty-five languages; the other counts the registered sound events and refuses new silent entries, grandfathering the ten legacy ones with their contribution still owed. The cabinet's memory now quietly mends impossible numbers in hand-edited or logically damaged records and keeps a documented migration road for future archives, while the ambience layer moved onto the same monotonic clockwork as the gramophone.


## Version 0.23.0 - The gramophone keeps the floor

The gramophone no longer shares the night with vanilla menu music: while the cabinet is open the old soundtrack stays silent, and if the sound engine is reloaded (F3+T or a pack change) the next piece queues by itself instead of falling forever quiet. The storm can now be tamed on its own: a new Reduced flashes aid dims lightning without freezing fog, rain or lamps, ambient events gain subtitles, and the ambience slider answers in the reader's own language.

The mod also learned manners: it registers its listener exactly once, stays inert on dedicated servers, sheds its empty mixin scaffolding and hides the debug shortcut unless summoned, and cedes the title screen whenever another mod already owns it. The chronicler now writes his memory off the render thread, and the build seals guard version sync and real track durations; a ready-to-enable continuous integration workflow (tools/ci) compiles every push with Java 17.


## Version 0.22.0 - The vigil is sealed

The city now moves to one steady twenty-beat pulse instead of advancing dust and ambience with every rendered frame. Reduced motion has a visible control among the perception aids and freezes atmospheric movement, grain and flashes without removing the street, lamps or any permanent scene detail. Every diary toggle now tells the narrator both its name and current state.

The chronicler no longer writes directly over the only memory file: each record is flushed to a temporary file, the last valid copy is preserved, replacement is atomic when supported, and a damaged record can be recovered without silently erasing the archive. Paper grain and city windows also reuse their deterministic instruments, reducing needless frame-by-frame allocations while preserving their appearance. The build seal now checks localization parity, current chronicles and the complete sound registry.


## Version 0.21.31 - The vigil remains whole

The city no longer measures its strength only to erase pieces of the night. Fog, gas lamps, their reflections and every street detail remain even when FPS falls. The old wooden kiosk that resembled a basket is gone as well, together with its daily Herald, hidden interaction and card. No old memory or erratic cycle can dim the lights again.


## Version 0.21.30 - The street remains

When the machine that sustains the vigil slows its pace, the city may lose its fog and dim its most demanding ornaments. The wet cobblestones, however, no longer vanish with them. Beneath the buildings, the street remains and the city keeps its ground.


## Version 0.21.25 - The ink is uniform

The chronicler signed with his own name. The director's hand was recognized. The city blueprints found words that fit within the margins. Every poster in the cabinet —elixir, decree, reward, quarantine— now speaks in the same dark ink, without the red shadow that once divided each title. The stamps no longer cast their own shadow. The logo, the pause signature, the shooting star and the easter egg telegram all shed their doubles. The visitor's book moved inside the panel where it belongs. The credits show the version number. The cabinet speaks with one voice.


## Version 0.21.13 - The file is sealed

The consultation room, when it finally gathers every card, stops saying that any are missing. The archive declares itself complete, a golden wax seal signs it with the word SIGILLUM, and the chronicler notes in the margin: forty-seven cards; the drawer is empty now; seal it. The bell tower rings once, low, so no one doubts it.


## Version 0.21.6 - The herald tells the cleansing

The kiosk newspaper found new words for the night: the district cabinet emptied of old papers, a roof lantern that flickered three times and went out on its own, the cat that came down to the kiosk and stole nothing. The fine print of the second column also changes from dawn to dawn. And whoever knows the word that opens the archive may ask the room to show the cards that are missing.


## Version 0.21.5 - The room answers the query

The consultation room learned to answer. Whoever searches among the cards by name, by hint, or by the word that names them, will see only those that matter; and when the archive confesses three ghosts of the cleansing, a fourth remembers the desk that was never used. The music yields for a moment when the archive speaks, and when the machine that holds the vigil runs slow, the street sheds its fog and lanterns so as not to stop.


## Version 0.21.4 - The ink settles

The cabinet put its tools in order. The field manual accepts being asked by name: whoever searches among its pages finds the control they need without turning over the rest. The observation apparatus divides its sections —what it paints, what it shows, what it measures— and adds a needle that reads the machine's pulse. The language consulates declare, as the hand passes, how many pages of their register the cabinet keeps. And in the consultation room, when the door opens, a paper falls to the floor.


## Version 0.21.3 - The cabinet breathes lighter

The cabinet learned not to waste its strength. The city repeats its clouds, its smoke and its rain from the same seed, as if the vigil kept itself in memory; and those who ask may quiet the motion of the scene: the lantern stops trembling, the moth settles, the rat stops crossing and the paper waits for the wind.

The archive confesses three things it removed in the cleansing: the archive that grows thinner, the theme that never played, and the street that never existed. Whoever speaks the word of the cleansing will receive a telegram from the chronicler himself.


## Version 0.21.2 - Deep cleanup

Following the cohesion pass and the sound registry fix, the cabinet was audited end-to-end. Historical audit docs, test samples, an unused 2.1M texture, inert theme framework and dead widgets were removed, and all docs were brought up to date. The result is a leaner codebase: 68 Java classes, 25 langs, 25 in-game changelogs, 42 OGG, 0 container PNGs.


## Version 0.21.1 - Sound registry correction + cleanup

The office did not change its appearance, but its internal discipline. The cabinet's sounds were registered more safely so the archive can open without stumbling at the start of the vigil. A deep cleanup followed: historical audit docs, test samples, an unused 2.1M texture, inert theme framework and dead widgets were removed.


## Version 0.21 - The office closes its ranks

The rooms that still felt most borrowed have now been brought nearer to the
rest of the cabinet. Correspondence, consultation, revisions and the forms
of entry now speak with a steadier hand, so that each office may be known by
its duty and not only by its heading.


## Version 0.20 - The cabinet finds its own hand

Several leaves that once felt merely dressed for the occasion have now
been rewritten in the cabinet's own hand. The consultation room, the
register of revisions, the forms of entry and authorisation, and the
small mirror of observation now belong more clearly to the same office,
with better order, better weight, and less borrowed manner.


## Version 0.18 - 0.19 - The cabinet learns its final manners

The house of the chronicler now presents itself with greater order and
less noise. The central desk, the room of instruments, the office of
correspondence, the linguistic archive and the ledgers of command no
longer look like borrowed sheets laid upon the same table. Each room has
its own bearing, yet all belong to the same night office.

Rules, needles, plates, rails of ink and papers laid one atop another now
show the eye where it ought to rest. The cabinet has not changed its
voice; it has learned how to keep it.


## Version 0.16 - The Reading Room

The cabinet is no longer a piece of furniture. It remembers.

From this night on, whatever the reader finds is written in a memory that
does not fade when the eyes close: the cards recovered, the small objects,
the nights in a row, the long absences. Dust gathers when no one returns,
and the desk knows how many hands opened it.

The chronicler no longer writes alone. He leaves notes in the margins when
something crosses him, corrects his own hand, greets the one who returns,
and keeps quiet when it suits him. Some swear the wood knocked back three
times.

The street can now be touched as well. The lamps go out by hand, the roof
cat pays its debts slowly, and when the bells toll it is wise to wait:
something crosses afterwards, very slowly, pulling a cart.

Behind the last folio of the chronicle there is a new room. Forty-three
cards. The missing ones are still out there.

## Version 0.15 - 0.15.2 - The craft returns to its wood

The steel plates that covered the workbenches have been removed. Furnaces,
chests, anvils and craft tables recover their old appearance: the plain
light grey of the common workshop, without rivets, without mouldings,
without seals. Every space is recognised at a glance again, as in the days
before the plague.

The house keeps its voice, its music and its records. Only the manual work
returns to the hands of the original craft.

That same night, a finer hand returned to the cabinet. The options desk
regained the field of view, which had slipped out of reach during the last
reorganisation, and the last hand-written letters over the work furniture
were erased. No label of our own remains over the boxes: only the words
the original craft writes.

The field of view, newly recovered, learned to state its number aloud:
every position of the slider now reads with its exact figure, as the craft
requires.



---


## Version 0.13.10 - Steel beneath the rain

The latest shipment arrived before dawn, covered in wet canvas and signed by
hands no one recognised. Inside the crates lay grey steel plates, clean rivets
and dark fittings for the work of a sickened city. From furnaces to lesser
boxes, from anvils to cutting tables, every trade received a sterner surface,
as if the cabinet had chosen to endure one more season of plague.

The correspondence rooms were revised as well. Headings lost needless stains,
lists were given room to breathe, and the pages no longer shone like white
glass beneath the lamp. Each latch and button was tuned with restraint: no
shriek, no trickery, only wood, paper and metal moving inside a closed house.

On the front ledger, the skull of the emblem no longer merely shivers. It now
hands out brief sealed files, difficult to explain, and leaves them in a safe
part of the desk where no button or notice can cover the reading. If the seal
appears, read it before the ink cools.

---

## Version 0.13 - The cabinet of machines

For several nights the workshops around the office were measured by cord,
ruler and candle. The carpenter wished to keep the wood; the smith demanded
steel; the chronicler, tired of argument, chose order. Thus came reinforced
tables, stricter furnace mouths and drawers that seem to have left the same
municipal factory.

The arcane table kept its purple vigil, though it now behaves less like
furniture and more like a relic. Its lines breathe tiny motes, an electric ash
that rises and vanishes before staining the fingers.

---

## Version 0.12 - Details in the gloom

The city began to move when no one watched. The moon learned phases, clouds
crossed its face, and small creatures claimed the streets without permission:
rats by the drains, cats on the cornices, moths circling the gas. They were
not ornaments; they were proof that the night was still at work.

Signatures also appeared beneath the papers. Old names, some struck through,
others barely legible, as though every chronicler had left a mark to warn the
next that the office never fully belongs to whoever occupies it.

---

## Version 0.10 - 0.11 - The house of paper

The cabinet finally abandoned its borrowed walls. Dirt-brown doors, soulless
frames and lights from another world were covered by parchment, sepia and worn
ink. Every room learned to resemble a page torn from a Victorian archive:
settings, tongues, sounds, controls, letters, credits and warnings all came
under the same lamp.

The register of lodgings was bound as well. Known houses, direct harbours, new
entries and painful removals came to live in a ledger, with seals, margins and
bureaucratic caution.

Sliders, writing fields and marginal notes stopped shouting. They began to
whisper. That was the true change: not a new door, but the correct silence
around each door.

---

## Version 0.9.2 - The ink learns obedience

The office received its first rules of reading. Pages stopped cutting lines
harshly, side bars gained their own ink, and small notices found a proper
border. The chronicler could turn the pages without feeling another trade had
invaded the desk.

---

## Version 0.9.1 - Bells of correction

There was a night of crossings-out. Repeated names, crooked seals and drawers
that would not close were revised until the minute lay clean. No one
celebrated: in quarantine, a proper correction is noticed precisely because it
stops troubling the eye.

---

## Version 0.9.0 - The chronicler's first office

Here the house first became recognisable. The main register received its
buttons, its discreet shadows and a period voice. Nails still showed in a few
places, but the room no longer felt temporary: it felt as though it was
waiting for a reader.

---

## Version 0.8.x - Lamps in the street

Before the complete cabinet came the city. Dark backdrop, gas lamps, rain,
distant sounds and the suspicion of buildings beyond the glass. The front page
stopped being a simple door and became a specific night.

---

## Version 0.7.x - First quarantine reports

The first papers were modest: notices, marks, seal tests and a name written
with an uncertain hand. There was no great archive yet, but there was a
promise: every improvement should feel like a document found in the middle of
the fever.
