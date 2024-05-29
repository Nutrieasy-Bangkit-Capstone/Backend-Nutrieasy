package nutrieasy.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Created by Resa S.
 * Date: 08-05-2024
 * Created in IntelliJ IDEA.
 */

@Configuration
@EnableWebSecurity
public class ConfigSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/login", "/token", "/*").permitAll() // Exclude login and token endpoints from authentication
                .anyRequest().authenticated();
    }


}
