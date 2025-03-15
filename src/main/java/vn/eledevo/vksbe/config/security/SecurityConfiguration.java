package vn.eledevo.vksbe.config.security;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import lombok.RequiredArgsConstructor;
import vn.eledevo.vksbe.constant.Role;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    //    private final TokenRepository tokenRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req.requestMatchers(PathUrlConfig.WHITE_LIST_URL)
                        .permitAll()
                        .requestMatchers(PathUrlConfig.ALL_ROLE_URL)
                        .hasAnyAuthority(
                                Role.IT_ADMIN.name(),
                                Role.KIEM_SAT_VIEN.name(),
                                Role.VIEN_PHO.name(),
                                Role.VIEN_TRUONG.name(),
                                Role.TRUONG_PHONG.name(),
                                Role.PHO_PHONG.name())
                        .requestMatchers(PathUrlConfig.NO_ROLE_KIEM_SAT_VIEN_URL)
                        .hasAnyAuthority(
                                Role.VIEN_TRUONG.name(),
                                Role.VIEN_PHO.name(),
                                Role.TRUONG_PHONG.name(),
                                Role.PHO_PHONG.name(),
                                Role.IT_ADMIN.name())
                        .requestMatchers(PathUrlConfig.NO_ROLE_KIEM_SAT_VIEN_AND_NO_IT_ADMIN_URL)
                        .hasAnyAuthority(
                                Role.VIEN_TRUONG.name(),
                                Role.VIEN_PHO.name(),
                                Role.TRUONG_PHONG.name(),
                                Role.PHO_PHONG.name())
                        .requestMatchers(PathUrlConfig.ROLE_IT_ADMIN_URL)
                        .hasAuthority(Role.IT_ADMIN.name())
                        .requestMatchers(PathUrlConfig.VIEN_TRUONG_VIEN_PHO_IT_ADMIN_URL)
                        .hasAnyAuthority(Role.VIEN_TRUONG.name(), Role.VIEN_PHO.name(), Role.IT_ADMIN.name())
                        .requestMatchers(PathUrlConfig.VIEN_TRUONG_VIEN_PHO_TRUONG_PHONG_URL)
                        .hasAnyAuthority(Role.VIEN_TRUONG.name(), Role.VIEN_PHO.name(), Role.TRUONG_PHONG.name())
                        .requestMatchers(PathUrlConfig.NO_ROLE_IT_ADMIN_URL)
                        .hasAnyAuthority(
                                Role.VIEN_TRUONG.name(),
                                Role.VIEN_PHO.name(),
                                Role.TRUONG_PHONG.name(),
                                Role.PHO_PHONG.name(),
                                Role.KIEM_SAT_VIEN.name())
                        .anyRequest()
                        .authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider) // Để xác thực người dùng trong lần đăng nhập
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter
                                .class) // Thêm bộ lọc xác thực JWT (Mỗi request đều được điều hướng đến doFilter để
                // kiểm tra)
                .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }));
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
