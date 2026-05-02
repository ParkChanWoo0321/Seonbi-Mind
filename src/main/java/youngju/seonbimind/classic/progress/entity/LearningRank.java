package youngju.seonbimind.classic.progress.entity;

import java.util.Arrays;
import java.util.Comparator;

public enum LearningRank {
    DOSA("도사", 0),
    JEONRANG("정랑", 1),
    CHEOMJEONG("첨정", 3),
    SAIN("사인", 6),
    JIBUI("집의", 10),
    CHAMUI("참의", 15),
    DONGJISA("동지사", 21),
    JISA("지사", 28),
    PANSA("판사", 36),
    YEONGUIJEONG("영의정", 45);

    private final String label;
    private final int threshold;

    LearningRank(String label, int threshold) {
        this.label = label;
        this.threshold = threshold;
    }

    public String getLabel() {
        return label;
    }

    public int getThreshold() {
        return threshold;
    }

    public static LearningRank fromTotalSolvedCount(int totalSolvedCount) {
        return Arrays.stream(values())
                .filter(rank -> totalSolvedCount >= rank.threshold)
                .max(Comparator.comparingInt(LearningRank::getThreshold))
                .orElse(DOSA);
    }

    public LearningRank next() {
        LearningRank[] values = values();
        int nextIndex = ordinal() + 1;
        if (nextIndex >= values.length) {
            return null;
        }
        return values[nextIndex];
    }
}
