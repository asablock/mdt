package io.github.asablock.mdt.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.ArrayList;
import java.util.List;

public final class CommandUtil {
    private CommandUtil() {
    }

    public static <S, T extends ArgumentBuilder<S, T>> ChainedCommandBuilder<S, T> chainedCommandBuilder(T first) {
        return new ChainedCommandBuilder<>(first);
    }

    public static class ChainedCommandBuilder<S, T extends ArgumentBuilder<S, T>> {
        private final List<Arg<?>> args;
        private final T first;

        private ChainedCommandBuilder(T first) {
            this.args = new ArrayList<>();
            this.first = first;
        }

        public <A> ChainedCommandBuilder<S, T> append(String name, A defaultValue, ArgumentType<A> type) {
            args.add(new Arg<>(name, defaultValue, type));
            return this;
        }

        public T executes(CommandWithArgs<S> command) {
            executes0(first, args, command);
            return first;
        }

        private static class Arg<A> extends ImmutableTriple<String, A, ArgumentType<A>> {
            public Arg(String s, A a, ArgumentType<A> aArgumentType) {
                super(s, a, aArgumentType);
            }
        }

        private static <S> void executes0(ArgumentBuilder<S, ?> first, final List<Arg<?>> args, final CommandWithArgs<S> command) {
            if (args.isEmpty()) {
                throw new IllegalArgumentException("args is empty");
            }

            final int size = args.size();
            final Object[] defaultVals = new Object[size];
            var iterator = args.iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                var a = iterator.next();
                defaultVals[i] = a.getMiddle();
            }

            RequiredArgumentBuilder<S, ?> builder = null;
            int i = size;
            for (Arg<?> arg : args.reversed()) {
                final int index = i;
                RequiredArgumentBuilder<S, ?> b = RequiredArgumentBuilder.argument(arg.getLeft(), arg.getRight());
                if (builder != null) b.then(builder);
                b.executes(context -> {
                    Object[] v = new Object[size];
                    for (int j = 0; j < index; j++) {
                        var a = args.get(j);
                        v[j] = context.getArgument(a.getLeft(), Object.class);
                    }
                    System.arraycopy(defaultVals, index, v, index, size - index);
                    return command.run(context, v);
                });
                builder = b;
                i--;
            }
            if (builder != null) first.then(builder);
            first.executes(context -> command.run(context, defaultVals));
        }

        public interface CommandWithArgs<S> {
            int SINGLE_SUCCESS = 1;

            int run(CommandContext<S> context, Object[] args) throws CommandSyntaxException;
        }
    }

    public static int success(boolean bl) {
        return bl ? Command.SINGLE_SUCCESS : 0;
    }
}
