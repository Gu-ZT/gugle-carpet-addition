package dev.dubhe.gugle.carpet.api.inject;

import dev.dubhe.gugle.carpet.tools.player.FakePlayerResident;

import javax.annotation.Nullable;

public interface IFakeResident {
    @Nullable
    FakePlayerResident getGCAResident();
}
