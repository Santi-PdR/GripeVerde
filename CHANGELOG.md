# Eternal Craft: Gripe Verde
## Notas de Actualizacion

## Version 0.8.3

**La musica se aparta cuando la ciudad habla.**

Cuando las campanas doblan a lo lejos, o un trueno hiende el cielo, o los cuervos alzan vuelo, la musica del menu se aparta con discrecion. No calla; solo baja la voz para que los presagios se escuchen con claridad. Cuando el momento pasa, la melodia vuelve suavemente al primer plano.

Tambien se pulieron las transiciones: entrar a un mundo, salir de un servidor, abrir Options, cambiar de escala â€” la musica ya no se corta ni se reinicia sin motivo.

### Sistema de musica del menu (fase 3)
- Ducking automatico cuando suenan campanas, truenos, cadenas o cuervos.
- La musica sobrevive a la apertura de Options y subpantallas.
- Al entrar a un mundo, la musica se detiene sola. Al volver, arranca de nuevo.
- Correccion de compilacion interna (variable duplicada).

---

## Version 0.8.2

**Las melodias se enlazan sin cortarse.**

Cuando una pieza termina, la siguiente ya esta comenzando a asomarse. Ambas se cruzan durante unos segundos â€” una que se despide, otra que llega â€” y la transicion se vuelve un respiro en vez de un salto.

### Sistema de musica del menu (fase 2)
- Crossfade suave de cuatro segundos entre pistas consecutivas.
- Sin cortes bruscos, sin silencios prolongados.
- Sistema interno reescrito para soportar dos instancias simultaneas.

---

## Version 0.8.1

**El volumen de la musica ahora responde a tu mano.**

Un nuevo control ha aparecido en la pantalla de Opciones, discreto, al pie de la pagina. Alli podras ajustar el volumen de la musica del menu sin salir del juego, y la melodia respondera en tiempo real â€” sin cortes, sin reinicios.

### Sistema de musica del menu (fase 1)
- Slider "Menu Music" en la pantalla de Opciones.
- Ajuste en tiempo real de 0% a 100%.
- Posicion segura para todas las escalas de interfaz.

---

## Version 0.8.0

**La ciudad tiene una banda sonora.**

Cuatro melodias han llegado al menu principal. Suenan en orden aleatorio, una tras otra, mezclandose con el viento y la lluvia sin taparlos. Cuando una termina, otra la sucede tras un breve silencio.

Es la primera vez que el diario tiene musica propia.

### Sistema de musica del menu (fase 0)
- Cuatro pistas de musica ambiental integradas al Title.
- Reproduccion secuencial aleatoria (shuffle).
- Volumen moderado por defecto, convivencia con los sonidos ambientales existentes.
- Preparado para expansion: slider, crossfade, ducking (versiones siguientes).

---
## Version 0.7.1

Las farolas se apagan por orden municipal.

La ciudad conserva ahora una calle definida bajo la niebla, pero los viejos postes de gas han sido retirados temporalmente del frente visual. En algunas escalas parecian flotar sin acera ni edificio que los sostuviera; era mejor apagar su luz que dejarla mentir.

Volveran cuando la silueta urbana tenga cimientos suficientes para recibirlos.

### Ajustes del Title
- Los postes/faroles quedan ocultos temporalmente en todas las escalas.
- La decision prepara el terreno para cerrar el vacio entre skyline y calle en la proxima fase.
- Se evita volver a mezclar sistemas interdependientes en un mismo cambio.

---

## Version 0.7.0

La calle vuelve a existir.

Bajo la lluvia y la niebla ocre, la ciudad por fin recupera una superficie reconocible: una calle oscura, mojada, de adoquines apenas visibles. No intenta resolver toda la perspectiva del skyline; solo devuelve al suelo su presencia.

El cambio fue deliberadamente pequeno: una calle decorativa adaptativa por escala, sin mover edificios ni postes.

### Fondo del Title
- Nueva calle decorativa en el nivel inferior del Title.
- Textura de adoquines, borde superior, reflejo humedo y peso oscuro en la base.
- Adaptacion por altura efectiva para que la calle lea mejor en escalas 1, 2, 3 y 4.
- Sin cambios en skyline, pipeline ni UI.

---

## Version 0.6.5

La ventana ya no castiga la mirada que se aparta.

Se corrigieron problemas de estabilidad al minimizar o cambiar el foco de la ventana. El diario puede quedar en segundo plano sin precipitar la sesion al desastre.

### Estabilidad
- Fix de crashes al perder foco de ventana o usar Alt+Tab.
- Base estable usada como punto de retorno antes de retomar KI-03.

---
# Eternal Craft: Gripe Verde
## Notas de ActualizaciÃƒÂ³n

## Version 0.6.2

**El reloj respira.**

Aquel viejo reloj de bolsillo, que durante tanto tiempo parecio un mero adorno, ahora late con un pulso mecanico suave. Cada cinco segundos, cuando el segundero alcanza una de las marcas negras, un chasquido discreto llega al oido Ã¢â‚¬â€ como si el mecanismo, largo tiempo olvidado, hubiese recordado cÃƒÂ³mo trabajar.

El sonido es apenas audible. Un susurro de bronce y engranajes. Pero una vez que lo adviertes, no puedes ignorarlo: el tiempo ha retomado su marcha.

### Un mecanismo vivo
- El segundero tickea en perfecta armonÃƒÂ­a con sus fallos Ã¢â‚¬â€ cuando tropieza, el sonido enmudece.
- El reloj de bolsillo solo suena cuando esta a la vista. En las ventanas mas pequeÃƒÂ±as, descansa en silencio.

---

## Version 0.6.1

**Algo se mueve sobre el escritorio.**

El reloj de bolsillo ha dejado de ser un retrato inmÃƒÂ³vil. Ahora cuelga de una cadena larga que desciende desde el mismÃƒÂ­simo borde superior del diario, y oscila con un lento pulso pendular, como si una corriente oculta lo acariciase.

Su segundero avanza con precisiÃƒÂ³n mecÃƒÂ¡nica Ã¢â‚¬â€ pero no siempre. De cuando en cuando tropieza: a veces se demora en una marca mÃƒÂ¡s de lo debido, a veces salta hacia adelante sin aviso, a veces retrocede un tiempo antes de continuar. El tiempo en esta ciudad, al parecer, tambiÃƒÂ©n estÃƒÂ¡ enfermo.

Un brillo diagonal se desliza lentamente sobre el cristal, como si una lÃƒÂ¡mpara lejana pasara frente a ÃƒÂ©l.

---

## VersiÃƒÂ³n 0.6.0

**La crÃƒÂ³nica cierra un capÃƒÂ­tulo y abre el siguiente.**

El diario ha sido puesto en orden. Cada pÃƒÂ¡gina, cada adorno, cada sonido Ã¢â‚¬â€ revisado y sellado. Lo que estaba disperso ahora estÃƒÂ¡ encuadernado.

Las pÃƒÂ¡ginas ahora se leen enteras: los pÃƒÂ¡rrafos mÃƒÂ¡s largos ya no se cortan al margen. Cada lÃƒÂ­nea encuentra su lugar en la hoja.

---

## VersiÃƒÂ³n 0.5.7

**Un reloj se detuvo a las once y cincuenta y dos.**

En la esquina superior derecha del diario ha aparecido un viejo reloj de bolsillo. Sus manecillas de bronce marcan siempre la misma hora Ã¢â‚¬â€ el instante en que todo se detuvo.

Debajo del reloj, tallada en pequeÃƒÂ±o, la crÃƒÂ³nica que llevas contigo.

### La ciudad respira mejor
- El diario ahora se ajusta con mÃƒÂ¡s gracia a cualquier ventana.
- Los avisos oficiales y el aviso de recompensa encuentran su lugar sin estorbar.
- En pantallas mÃƒÂ¡s pequeÃƒÂ±as, las pÃƒÂ¡ginas se simplifican por sÃƒÂ­ solas, mostrando solo lo esencial.

### Un detalle en el margen
Al pie de la ÃƒÂºltima pÃƒÂ¡gina, si observas con atenciÃƒÂ³n, encontrarÃƒÂ¡s una firma escrita a mano en tinta sepia. Anno Domini MDCCCXCVII.

---

## VersiÃƒÂ³n 0.5.0

**El diario recuerda cada paso del camino.**

Ahora podrÃƒÂ¡s consultar el historial completo de novedades directamente desde el menÃƒÂº principal. Un discreto sÃƒÂ­mbolo aparece en la esquina superior derecha, esperando ser descubierto.

Al abrirlo, encontrarÃƒÂ¡s todas las noticias que han llegado desde los primeros dÃƒÂ­as de la epidemia hasta el presente.

- NavegaciÃƒÂ³n con rueda del mouse, flechas o las teclas de pÃƒÂ¡gina.
- DiseÃƒÂ±o en pergamino, fiel al espÃƒÂ­ritu de la ciudad.

---

## VersiÃƒÂ³n 0.4.4

**La ciudad ha cobrado vida.**

Las calles ya no permanecen en silencio. Al asomarte al menÃƒÂº principal podrÃƒÂ¡s oÃƒÂ­r el pulso de una urbe atrapada por la Gripe Verde.

### Voces de la ciudad
- El viento recorre las callejuelas empedradas.
- La lluvia repica sobre los adoquines.
- Un murmullo distante se alza desde los tejados.

### Sombras que se mueven
- Pasos apurados se pierden en la niebla.
- Maderas viejas crujen bajo un peso invisible.
- Una puerta se abre en algÃƒÂºn lugar lejano.

### Presagios
- Las campanas de la iglesia repican sin razÃƒÂ³n aparente.
- Un trueno rompe el cielo cargado de humo.
- Los cuervos anuncian lo que nadie quiere ver.
- El eco de unas cadenas asciende desde los sÃƒÂ³tanos.

### Voces en muchas lenguas
La cuarentena no distingue fronteras. El diario ahora habla en veinticinco lenguas, para que ningÃƒÂºn alma quede sin comprender el aviso.

---

## VersiÃƒÂ³n 0.2.5

**El diario luce mÃƒÂ¡s vivo que nunca.**

Las pÃƒÂ¡ginas han sido restauradas con esmero. Cada detalle busca transmitir la atmÃƒÂ³sfera de una ciudad en tinieblas.

- Las velas iluminan con un halo cÃƒÂ¡lido y tembloroso.
- Las manchas de sangre se muestran con crudeza sobre el papel.
- Los sellos de cera lucen con relieve y sombra.
- Adornos florales enmarcan las secciones mÃƒÂ¡s importantes.
- El papel envejecido cubre las pÃƒÂ¡ginas sin cortes ni bordes.

### Nuevos trazos
- Manchas de tinta corrida.
- Trazos de pluma bajo los tÃƒÂ­tulos.
- Bordes rasgados en algunas hojas.
- Un modo especial para las escenas mÃƒÂ¡s sombrÃƒÂ­as.

---

## VersiÃƒÂ³n 0.2.0

**El diario abre sus primeras pÃƒÂ¡ginas.**

El papel envejecido, la tinta y los adornos empiezan a dar forma a lo que serÃƒÂ¡ la crÃƒÂ³nica completa de la epidemia.

---

## VersiÃƒÂ³n 0.1.0

**Todo comienza aquÃƒÂ­.**

La ciudad despierta bajo las primeras seÃƒÂ±ales de la Gripe Verde. El diario abre su cubierta por primera vez.