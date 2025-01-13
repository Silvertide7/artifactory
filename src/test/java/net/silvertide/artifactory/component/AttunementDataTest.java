package net.silvertide.artifactory.component;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AttunementDataTest {

    @Test
    void testCreateAttunementData() {
        UUID itemAttunementUUID = UUID.randomUUID();
        UUID attunedToUUID = UUID.randomUUID();
        AttunementData attunementData = new AttunementData(
                itemAttunementUUID,
                attunedToUUID,
                "Spongebob",
                true,
                false,
                false,
                new ArrayList<>()
        );

        AttunementData attunementData2 = new AttunementData(
                itemAttunementUUID,
                attunedToUUID,
                "Spongebob",
                true,
                false,
                false,
                new ArrayList<>()
        );

        AttunementData attunementData3 = new AttunementData(
                itemAttunementUUID,
                attunedToUUID,
                "Spongebob1",
                false,
                false,
                false,
                new ArrayList<>()
        );

        assertEquals(attunementData, attunementData2);
        assertNotEquals(attunementData, attunementData3);
    }

}
