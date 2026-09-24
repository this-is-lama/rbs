package my.project.telegrambotservice.support.dialog;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import my.project.telegrambotservice.config.SupportProperties;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Черновики обращений по chat id. Живут в памяти: если пользователь молчит дольше {@code app.support.draft-ttl}
 * или сервис перезапустился, черновик теряется и обращение нужно начать заново. Готовые обращения хранятся в БД.
 */
@Component
public class DraftStorage {

	private final Cache<Long, SupportDraft> drafts;

	public DraftStorage(SupportProperties properties) {
		this.drafts = Caffeine.newBuilder()
				.expireAfterAccess(properties.draftTtl())
				.maximumSize(10_000)
				.build();
	}

	public Optional<SupportDraft> find(long chatId) {
		return Optional.ofNullable(drafts.getIfPresent(chatId));
	}

	public SupportDraft create(long chatId) {
		SupportDraft draft = new SupportDraft();
		drafts.put(chatId, draft);
		return draft;
	}

	/** Возвращает черновик на место, если отправить обращение не получилось. */
	public void put(long chatId, SupportDraft draft) {
		drafts.put(chatId, draft);
	}

	/** Удаляет черновик и возвращает его — так повторное нажатие «Отправить» не создаст второе обращение. */
	public Optional<SupportDraft> remove(long chatId) {
		return Optional.ofNullable(drafts.asMap().remove(chatId));
	}
}
