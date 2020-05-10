package study.seop.restapi.accounts;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void findByUsername() {

        // Given
        String password = "1234";
        String username = "edenhazard5870@gmail.com";
        Account account = Account.builder()
                    .email(username)
                    .password(password)
                    .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                    .build();

        accountService.saveAccount(account);
        //accountRepository.save(account);

        // When
        UserDetailsService userDetailsService = (UserDetailsService) accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        //assertThat(userDetails.getPassword()).isEqualTo(password);
        assertThat(passwordEncoder.matches(password,userDetails.getPassword()));
    }

    @Test
    public void findByUsernameFail() {
        // Expected
        String username = "none";
        // 실제 Exception이 발생하는 로직보다 먼저 예상되는 예외를 먼저 처리해줘야함 서비스 로직을 먼저 타게 되면 예상되는 Exception
        // 처리를 못했기 때문에 테스트가 깨짐
        expectedException.expect(UsernameNotFoundException.class);
        expectedException.expectMessage(Matchers.containsString(username));

        // When
        accountService.loadUserByUsername(username);
    }
}