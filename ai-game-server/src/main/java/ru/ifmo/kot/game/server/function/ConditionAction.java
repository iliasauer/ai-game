package ru.ifmo.kot.game.server.function;
import org.apache.logging.log4j.util.Supplier;
import ru.ifmo.kot.protocol.function.Action;

@FunctionalInterface
public interface ConditionAction {
    void execute(final Supplier<Boolean> condition,
                 final Action trueAction, final Action falseAction);

    static ConditionAction newAction() {
        return (cond, tAction, fAction) -> {
            if (cond.get()) {
                tAction.execute();
            } else {
                fAction.execute();
            }
        };
    }
}
