package br.com.gabryel.waes.rdc.banking.matchers;

import br.com.gabryel.waes.rdc.banking.model.entity.IdHolder;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static br.com.gabryel.waes.rdc.banking.helper.NullUtils.firstNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UtilityClass
public class CustomMocks {
    public static <V extends IdHolder<UUID>, T extends JpaRepository<V, UUID>> void configureRepositoryMock(T mock) {
        var cache = new HashMap<UUID, V>();

        when(mock.save(any())).thenAnswer((inv) -> {
            V item = inv.getArgument(0);
            item.setId(firstNonNull(item.getId(), UUID.randomUUID()));

            cache.put(item.getId(), item);
            return item;
        });

        when(mock.findById(any())).thenAnswer((inv) ->
            Optional.ofNullable(cache.get(inv.<UUID>getArgument(0))));
    }
}
