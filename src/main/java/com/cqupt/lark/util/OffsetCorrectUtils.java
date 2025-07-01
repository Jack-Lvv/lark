package com.cqupt.lark.util;

import com.cqupt.lark.translation.model.entity.TestCaseVision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OffsetCorrectUtils {
    private static int xOffset;
    @Value("${offset.xOffset:0}")
    public void setXOffset(int xOffset) {
        OffsetCorrectUtils.xOffset = xOffset;
    }
    private static int yOffset;
    @Value("${offset.yOffset:0}")
    public void setYOffset(int yOffset) {
        OffsetCorrectUtils.yOffset = yOffset;
    }
    private static double xRate;
    @Value("${offset.xRate:1}")
    public void setXRate(double xRate) {
        OffsetCorrectUtils.xRate = xRate;
    }
    private static double yRate;
    @Value("${offset.yRate:1}")
    public void setYRate(double yRate) {
        OffsetCorrectUtils.yRate = yRate;
    }
    public static TestCaseVision correct(TestCaseVision offset) {

        return TestCaseVision.builder()
                .caseType(offset.getCaseType())
                .caseValue(offset.getCaseValue())
                .xUp((int)(offset.getXUp() * xRate) + xOffset)
                .yUp((int)(offset.getYUp() * yRate) + yOffset)
                .xDown((int)(offset.getXDown() * xRate) + xOffset)
                .yDown((int)(offset.getYDown() * yRate) + yOffset)
                .build();
    }
}
