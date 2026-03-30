package com.proyectojpa.demo.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrae el id de video de YouTube para iframes embebidos.
 */
public final class YoutubeUrlUtil {

    private static final Pattern[] PATTERNS = {
            Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)([a-zA-Z0-9_-]{11})"),
            Pattern.compile("youtube\\.com/shorts/([a-zA-Z0-9_-]{11})")
    };

    private YoutubeUrlUtil() {
    }

    /** Devuelve la URL de embed o null si no es reconocible. */
    public static String embedUrl(String url) {
        String id = extractVideoId(url);
        return id != null ? "https://www.youtube.com/embed/" + id : null;
    }

    public static String extractVideoId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String s = url.trim();
        for (Pattern p : PATTERNS) {
            Matcher m = p.matcher(s);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    public static boolean pareceUrlYoutube(String url) {
        return url != null && (url.contains("youtube.com") || url.contains("youtu.be"));
    }
}
