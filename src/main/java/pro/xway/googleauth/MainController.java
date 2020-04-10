package pro.xway.googleauth;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;

@Controller
@RequestMapping("/")
public class MainController {

    public static final String SECRET_KEY = "DANXUXQ5WH7M3SLJXGUMYKVX2EWPNFNT";

    @GetMapping
    public String index(Model model) throws IOException, WriterException {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(SECRET_KEY);
        String hexKey = Hex.encodeHexString(bytes);
        String otp = TOTP.getOTP(hexKey);
        System.out.println(otp);

        model.addAttribute("generatedImageText", create());

        return "index";
    }

    @PostConstruct
    public String create() throws IOException, WriterException {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
//        String encode = base32.encodeToString(bytes);
        String encode = SECRET_KEY;

        String barCodeUrl = getGoogleAuthenticatorBarCode(encode, "test@test.ru", "ecp");
        System.out.println(encode);
        byte[] qrCode = createQRCode(barCodeUrl, "./test.png", 500, 500);

        return Base64.encodeBase64String(qrCode);
    }

    public static byte[] createQRCode(String barCodeData, String filePath, int height, int width)
            throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter()
                .encode(barCodeData, BarcodeFormat.QR_CODE, width, height);
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            MatrixToImageWriter.writeToStream(matrix, "png", out);
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(matrix, "png", out);
            return out.toByteArray();
        }

    }

    public static String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) throws UnsupportedEncodingException {
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }


}
