package net.shrine.webclient.shared.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class And implements Expression {

    private final List<Andable> components = Util.makeArrayList();

    public And(final Collection<? extends Andable> components) {
        super();

        Util.requireNotNull(components);

        this.components.addAll(components);
    }

    public And(final Andable... components) {
        this(Arrays.asList(components));
    }

    public List<Andable> getComponents() {
        return Util.makeArrayList(components);
    }

    @Override
    public Collection<Term> getTerms() {
        final Collection<Term> result = Util.makeArrayList();

        for (final Expression expr : components) {
            result.addAll(expr.getTerms());
        }

        return result;
    }

    public And with(final Andable component) {
        Util.requireNotNull(component);

        if (this.components.contains(component)) {
            return this;
        }

        final List<Andable> newComponents = Util.makeArrayList(components);

        newComponents.add(component);

        return new And(newComponents);
    }

    public And without(final Andable component) {
        Util.requireNotNull(component);

        if (!components.contains(component)) {
            return this;
        }

        final List<Andable> newComponents = Util.makeArrayList(components);

        newComponents.remove(component);

        return new And(newComponents);
    }

    @Override
    public String toXmlString() {
        final List<String> componentXmls = Util.makeArrayList();

        for (final Expression comp : components) {
            componentXmls.add(comp.toXmlString());
        }

        return "<and>" + Util.join(componentXmls) + "</and>";
    }

    @Override
    public String toString() {
        return "And [components=" + components + "]";
    }
}
