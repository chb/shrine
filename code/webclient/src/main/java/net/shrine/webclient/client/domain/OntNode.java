package net.shrine.webclient.client.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author clint
 * @date Apr 2, 2012
 */
public final class OntNode implements IsSerializable {
	private String value;

	private String simpleName;

	private boolean isLeaf;

	private List<OntNode> children;

	// For GWT
	@SuppressWarnings("unused")
	private OntNode() {
		this(null, null, new ArrayList<OntNode>(), false);
	}

	public OntNode(final String value, final String simpleName, final boolean isLeaf) {
		this(value, simpleName, Collections.<OntNode> emptyList(), isLeaf);
	}

	public OntNode(final String value, final String simpleName, final List<OntNode> children, final boolean isLeaf) {
		super();

		this.value = value;
		this.simpleName = simpleName;
		this.children = children;
		this.isLeaf = isLeaf;
	}

	public String getValue() {
		return value;
	}

	public List<OntNode> getChildren() {
		return children;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public void setChildren(final List<OntNode> children) {
		this.children = children;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public void setSimpleName(final String simpleName) {
		this.simpleName = simpleName;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(final boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	@Override
	public String toString() {
		return "OntNode['" + value + "' leaf? " + isLeaf + " (" + simpleName + ") " + children + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (children == null ? 0 : children.hashCode());
		result = prime * result + (isLeaf ? 1231 : 1237);
		result = prime * result + (simpleName == null ? 0 : simpleName.hashCode());
		result = prime * result + (value == null ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final OntNode other = (OntNode) obj;
		if (children == null) {
			if (other.children != null) {
				return false;
			}
		} else if (!children.equals(other.children)) {
			return false;
		}
		if (isLeaf != other.isLeaf) {
			return false;
		}
		if (simpleName == null) {
			if (other.simpleName != null) {
				return false;
			}
		} else if (!simpleName.equals(other.simpleName)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
}
