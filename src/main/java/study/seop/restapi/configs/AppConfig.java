package study.seop.restapi.configs;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import study.seop.restapi.accounts.Account;
import study.seop.restapi.accounts.AccountService;
import study.seop.restapi.accounts.AccountRole;

import java.util.Set;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

//    @Bean
//    public ApplicationRunner applicationRunner() {
//        return new ApplicationRunner() {
//
//            @Autowired
//            AccountService accountService;
//
//            @Override
//            public void run(ApplicationArguments args) throws Exception {
//                Account account = Account.builder()
//                        .email("edenhazard5870@gmail.com")
//                        .password("1234")
//                        .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
//                        .build();
//                accountService.saveAccount(account);
//            }
//        };
//    }
}
