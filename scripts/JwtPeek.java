import java.util.Base64;

// Peeks into a JWT token without any library. A token is three parts separated by dots,
// each encoded with Base64URL: header, payload and signature. Anyone holding the token can
// read the first two, so a token never carries secrets. This program does not check the
// signature: the application does that with its public key.
// Run from the project directory: java scripts/JwtPeek.java <token>
public class JwtPeek {

    public static void main(String[] args) {
        String[] parts = args[0].split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        System.out.println("header:    " + new String(decoder.decode(parts[0])));
        System.out.println("payload:   " + new String(decoder.decode(parts[1])));
        System.out.println("signature: " + parts[2].length() + " Base64URL characters");
    }
}
