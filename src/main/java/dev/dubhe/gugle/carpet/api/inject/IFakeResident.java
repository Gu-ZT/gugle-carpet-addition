package dev.dubhe.gugle.carpet.api.inject;

import dev.dubhe.gugle.carpet.tools.player.FakePlayerResident;
import org.jetbrains.annotations.Nullable;

public interface IFakeResident {
    @Nullable
    FakePlayerResident getGCAResident();
}
