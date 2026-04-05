package com.proyectojpa.demo.Service;

/**
 * Plantilla HTML común para correos transaccionales (misma estética que pago pendiente).
 */
public final class EmailLayoutInstitucional {

    private EmailLayoutInstitucional() {
    }

    public static String baseUrlSinSlashFinal(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8080";
        }
        String t = url.trim();
        return t.endsWith("/") ? t.substring(0, t.length() - 1) : t;
    }

    /**
     * Página completa: cabecera negra, cuerpo, botón redondeado y pie (como pago pendiente).
     *
     * @param tituloH2       texto del encabezado de sección (puede incluir entidades HTML)
     * @param cuerpoInterno  HTML del bloque principal (párrafos, cajas, etc.)
     * @param textoBoton     etiqueta del botón de acción
     * @param urlBoton       URL absoluta del botón (debe basarse en {@code app.public-url})
     */
    public static String pagina(String tituloH2, String cuerpoInternoHtml, String textoBoton, String urlBoton) {
        String href = urlBoton == null || urlBoton.isBlank() ? "#" : urlBoton.trim();
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f4f4f4; font-family: 'Segoe UI', Arial, sans-serif;">
                    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 12px; overflow: hidden; margin-top: 30px; margin-bottom: 30px; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">

                        <tr>
                            <td align="center" style="background-color: #000000; padding: 40px 20px; border-bottom: 4px solid #6619f5;">
                                <h1 style="color: #ffffff; margin: 0; font-size: 28px; letter-spacing: 1px;">Sabor MasterClass</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding: 40px 30px;">
                                <h2 style="color: #333333; margin-top: 0; font-size: 22px;">%s</h2>

                                %s

                                <p style="color: #333333; font-weight: bold; font-size: 17px; text-align: center; margin-top: 30px; margin-bottom: 10px;">
                                    &#127859; ¡¡Sabor Master Class sabemos lo que hacemos!!
                                </p>

                                <table align="center" border="0" cellpadding="0" cellspacing="0" style="margin-top: 20px;">
                                    <tr>
                                        <td align="center" style="border-radius: 50px; background-color: #000000;">
                                            <a href="%s" style="display: inline-block; padding: 12px 35px; color: #ffffff; text-decoration: none; font-weight: bold; border-radius: 50px; font-size: 14px;">
                                                %s
                                            </a>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>

                        <tr>
                            <td align="center" style="background-color: #000000; padding: 30px 20px; color: #aaaaaa; font-size: 12px;">
                                <p style="margin: 0; color: #ffffff;">Sabor MasterClass © 2025</p>
                                <p style="margin: 5px 0 0 0;">Todos los derechos reservados. Desarrollado por HAMN</p>
                                <div style="margin-top: 15px;">
                                    <span style="color: #6619f5;">&#8226;</span> Facebook <span style="color: #6619f5;">&#8226;</span> Instagram <span style="color: #6619f5;">&#8226;</span> Twitter
                                </div>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>"""
                .formatted(tituloH2, cuerpoInternoHtml, href, textoBoton);
    }
}
