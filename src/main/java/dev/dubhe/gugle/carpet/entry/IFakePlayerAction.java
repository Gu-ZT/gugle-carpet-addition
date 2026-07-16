package dev.dubhe.gugle.carpet.entry;

import carpet.helpers.EntityPlayerActionPack;
import dev.dubhe.gugle.carpet.mixin.APAccessor;

public interface IFakePlayerAction {

    boolean getSneaking();

    boolean getSprinting();

    float getForward();

    float getStrafing();

    void applyAction(EntityPlayerActionPack actionPack);

    static IFakePlayerAction of(EntityPlayerActionPack actionPack) {
        APAccessor accessor = (APAccessor) actionPack;
        return new FakePlayerActionPackImpl(accessor);
    }

    class FakePlayerActionPackImpl implements IFakePlayerAction {
        private final APAccessor accessor;

        public FakePlayerActionPackImpl(APAccessor accessor) {
            this.accessor = accessor;
        }

        @Override
        public boolean getSneaking() {
            return this.accessor.getSneaking();
        }

        @Override
        public boolean getSprinting() {
            return this.accessor.getSprinting();
        }

        @Override
        public float getForward() {
            return this.accessor.getForward();
        }

        @Override
        public float getStrafing() {
            return this.accessor.getStrafing();
        }

        @Override
        public void applyAction(EntityPlayerActionPack actionPack) {
            accessor.getActions().forEach((type, action) -> {
                if (action.done) return;
                actionPack.start(type, action);
            });
        }
    }
}
