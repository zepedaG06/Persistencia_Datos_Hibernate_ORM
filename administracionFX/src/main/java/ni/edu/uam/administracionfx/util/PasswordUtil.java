package ni.edu.uam.administracionfx.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de contraseñas con PBKDF2-HMAC-SHA256 (incluido en el JDK, sin dependencias).
 * Formato guardado: pbkdf2$iteraciones$salBase64$hashBase64
 */
public final class PasswordUtil {

    private static final String PREFIJO = "pbkdf2";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_SAL = 16;
    private static final int LONGITUD_HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String password) {
        byte[] sal = new byte[LONGITUD_SAL];
        RANDOM.nextBytes(sal);
        byte[] hash = pbkdf2(password.toCharArray(), sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    public static boolean verificar(String password, String guardado) {
        if (password == null || guardado == null) return false;
        if (!esHash(guardado)) {
            // Usuarios antiguos guardados en texto plano.
            return MessageDigest.isEqual(password.getBytes(), guardado.getBytes());
        }
        String[] partes = guardado.split("\\$");
        if (partes.length != 4) return false;
        int iteraciones = Integer.parseInt(partes[1]);
        byte[] sal = Base64.getDecoder().decode(partes[2]);
        byte[] esperado = Base64.getDecoder().decode(partes[3]);
        byte[] calculado = pbkdf2(password.toCharArray(), sal, iteraciones);
        return MessageDigest.isEqual(esperado, calculado);
    }

    public static boolean esHash(String guardado) {
        return guardado != null && guardado.startsWith(PREFIJO + "$");
    }

    private static byte[] pbkdf2(char[] password, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, sal, iteraciones, LONGITUD_HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }
}
