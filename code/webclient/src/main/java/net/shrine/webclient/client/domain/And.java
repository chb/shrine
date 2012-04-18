package net.shrine.webclient.client.domain;

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
	
	private static final long serialVersionUID = 1L;
	
	private final List<Expression> components = Util.makeArrayList();

	public And(final Collection<? extends Expression> components) {
		super();

		Util.requireNotNull(components);

		this.components.addAll(components);
	}

	public And(final Expression ... components) {
		this(Arrays.asList(components));
	}

	public List<Expression> getComponents() {
		return Util.makeArrayList(components);
	}
	
	@Override
	public Collection<Term> getTerms() {
		final Collection<Term> result = Util.makeArrayList();
		
		for(final Expression expr : components) {
			result.addAll(expr.getTerms());
		}
		
		return result;
	}

	public And with(final Andable component) {
		Util.requireNotNull(component);

		if(this.components.contains(component)) {
			return this;
		}
		
		final List<Expression> newComponents = Util.makeArrayList(components);
		
		newComponents.add(component);
		
		return new And(newComponents);
	}
	
	public And without(final Andable component) {
		Util.requireNotNull(component);

		if(!components.contains(component)) {
			return this;
		}
		
		final List<Expression> newComponents = Util.makeArrayList(components);
		
		newComponents.remove(component);
		
		return new And(newComponents);
	}
	
	@Override
	public String toXmlString() {
		return "<and>" + Util.join("", components) + "</and>";
	}
}
