package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Сообщение Telegram. Из вложений бот не разбирает содержимое — только определяет тип,
 * а сами файлы пересылает через copyMessage.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(
		@JsonProperty("message_id") long messageId,
		User from,
		Chat chat,
		String text,
		String caption,
		@JsonProperty("media_group_id") String mediaGroupId,
		@JsonProperty("reply_to_message") Message replyToMessage,
		List<Object> photo,
		Object document,
		Object video,
		Object animation,
		Object audio,
		Object voice,
		Object sticker,
		@JsonProperty("video_note") Object videoNote
) {

	public ContentType contentType() {
		if (text != null) return ContentType.TEXT;
		if (photo != null && !photo.isEmpty()) return ContentType.PHOTO;
		if (document != null) return ContentType.DOCUMENT;
		if (video != null) return ContentType.VIDEO;
		if (animation != null) return ContentType.ANIMATION;
		if (audio != null) return ContentType.AUDIO;
		if (voice != null) return ContentType.VOICE;
		if (sticker != null) return ContentType.STICKER;
		if (videoNote != null) return ContentType.VIDEO_NOTE;
		return ContentType.OTHER;
	}

	/** Текст сообщения или подпись к вложению. */
	public String textOrCaption() {
		return text != null ? text : caption;
	}

	public enum ContentType {
		TEXT, PHOTO, DOCUMENT, VIDEO, ANIMATION, AUDIO, VOICE, STICKER, VIDEO_NOTE, OTHER;

		/** Вложения, которые можно приложить к обращению как скриншот или файл. */
		public boolean isAttachment() {
			return this == PHOTO || this == DOCUMENT || this == VIDEO;
		}

		/** Типы, у которых в Telegram бывает подпись (caption). */
		public boolean supportsCaption() {
			return this == PHOTO || this == DOCUMENT || this == VIDEO || this == ANIMATION || this == AUDIO || this == VOICE;
		}
	}
}
