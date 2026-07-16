package com.eternalcraft.gripeverde.client.theme;

/**
 * Base común de temas.
 *
 * NO contiene lógica visual. Los temas delegan todo el dibujo a los
 * *Renderer del paquete components. Esta clase existe solo para
 * compartir metadata futura entre temas si hiciera falta.
 */
public abstract class AbstractScreenTheme implements ScreenTheme {
    // Vacío intencionalmente. Los temas usan directamente los Renderer.
}