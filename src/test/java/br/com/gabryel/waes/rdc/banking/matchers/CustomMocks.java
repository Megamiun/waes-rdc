package br.com.gabryel.waes.rdc.banking.matchers;

import br.com.gabryel.waes.rdc.banking.model.entity.EntityWithId;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UtilityClass
public class CustomMocks {
    public static <U, V extends EntityWithId<U>, T extends JpaRepository<V, U>> void configureRepositoryMock(T mock) {
        var cache = new HashMap<U, V>();

        when(mock.save(any())).thenAnswer((inv) -> {
            V account = inv.getArgument(0);
            cache.put(account.getId(), account);
            return account;
        });

        when(mock.findById(any())).thenAnswer((inv) ->
            cache.get(inv.<U>getArgument(0)
            ));
    }
}
