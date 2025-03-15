package vn.eledevo.vksbe.config.security;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    /**
     * Khóa bí mật được sử dụng để tạo và xác minh chữ ký của JWT.
     * Giá trị của khóa này được lấy từ biến môi trường.
     */
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Thời gian hết hạn của access token JWT (tính bằng mili giây).
     * Giá trị này được lấy từ biến môi trường.
     */
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Thời gian hết hạn của refresh token JWT (tính bằng mili giây).
     * Giá trị này được lấy từ biến môi trường.
     */
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /**
     * Trích xuất tên người dùng (subject) từ JWT token.
     *
     * @param token JWT token
     * @return Tên người dùng
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    /**
     * Trích xuất một claim cụ thể từ JWT token.
     *
     * @param token          JWT token
     * @param claimsResolver Hàm lambda để trích xuất claim từ đối tượng Claims
     * @param <T>            Kiểu dữ liệu của claim
     * @return Giá trị của claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Tạo một access token JWT mới cho người dùng.
     *
     * @param userDetails Thông tin của người dùng
     * @return Access token JWT
     */
    public String generateToken(UserDetails userDetails, UUID keyUsb, String role) {
        return generateToken(new HashMap<>(), userDetails, keyUsb, role);
    }

    /**
     * Tạo một access token JWT mới cho người dùng với các claim bổ sung.
     *
     * @param extraClaims Các claim bổ sung
     * @param userDetails Thông tin của người dùng
     * @return Access token JWT
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, UUID keyUsb, String role) {
        return buildToken(extraClaims, userDetails, jwtExpiration, keyUsb, role);
    }

    /**
     * Tạo một refresh token JWT mới cho người dùng.
     *
     * @param userDetails Thông tin của người dùng
     * @return Refresh token JWT
     */
    public String generateRefreshToken(UserDetails userDetails, UUID keyUsb, String role) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration, keyUsb, role);
    }

    /**
     * Tạo một JWT token mới với các claim và thời gian hết hạn được chỉ định.
     *
     * @param extraClaims Các claim bổ sung
     * @param userDetails Thông tin của người dùng
     * @param expiration  Thời gian hết hạn của token (tính bằng mili giây)
     * @param keyUsb
     * @return JWT token
     */
    private String buildToken(
            Map<String, Object> extraClaims, // Đối tượng map
            UserDetails userDetails, // Đối tượng là userDetails
            long expiration, // Thời gian tồn tại của token
            UUID keyUsb,
            String role) {
        extraClaims.put("keyUsb", keyUsb);
        extraClaims.put("role", role);
        return Jwts.builder()
                .setClaims(extraClaims) // Khởi tạo 1 object payload
                // thêm các giá trị vào bên trong payload
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // thêm chữ ký và thuật toán dùng cho token
                .compact(); // tạo ra token
    }

    /**
     * Kiểm tra tính hợp lệ của JWT token dựa trên tên người dùng và thời gian hết hạn.
     *
     * @param token       JWT token
     * @param userDetails Thông tin của người dùng
     * @return True nếu token hợp lệ, false nếu không
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Kiểm tra xem JWT token đã hết hạn hay chưa.
     *
     * @param token JWT token
     * @return True nếu token đã hết hạn, false nếu còn hạn
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = extractExpiration(token);
            return expirationDate.before(new Date()); // So sánh với thời gian hiện tại
        } catch (ExpiredJwtException e) {
            System.out.println("Token đã hết hạn: " + e.getMessage());
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi giải mã token: " + e.getMessage());
            return true; // Nếu có lỗi khác, xem như token không hợp lệ
        }
    }

    /**
     * Trích xuất thời gian hết hạn từ JWT token.
     *
     * @param token JWT token
     * @return Thời gian hết hạn của token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Trích xuất tất cả các claim (claims) từ JWT token.
     *
     * @param token JWT token
     * @return Đối tượng Claims chứa các claim
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    /**
     * Tạo khóa ký cho JWT token từ khóa bí mật (secret key).
     *
     * @return Khóa ký cho JWT token
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getHasStringFromToken(String token) {
        Claims claims =
                Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

        return claims.get("hasString", String.class);
    }
}
