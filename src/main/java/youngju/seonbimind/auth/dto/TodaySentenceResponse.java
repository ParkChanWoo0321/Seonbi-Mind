package youngju.seonbimind.auth.dto;

import youngju.seonbimind.classic.sentence.entity.ClassicSentence;

public record TodaySentenceResponse(
        Long sentenceId,
        String originalText,
        String readingText,
        String meaning
) {

    public static TodaySentenceResponse from(ClassicSentence sentence) {
        return new TodaySentenceResponse(
                sentence.getId(),
                sentence.getOriginalText(),
                sentence.getReadingText(),
                sentence.getMeaning()
        );
    }
}
