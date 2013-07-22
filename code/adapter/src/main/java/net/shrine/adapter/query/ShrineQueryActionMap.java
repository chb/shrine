package net.shrine.adapter.query;

import org.spin.node.DestroyableQueryActionMap;
import org.spin.node.UnknownQueryTypeException;
import org.spin.node.actions.QueryAction;

import java.util.Collection;
import java.util.Map;


/**
 * @author Bill Simons
 * @date Jul 9, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public final class ShrineQueryActionMap extends DestroyableQueryActionMap
{
    private final Map<String, QueryAction<?>> map;

    public ShrineQueryActionMap(final Map<String, QueryAction<?>> actionMap)
    {
        this.map = actionMap;
    }

    @Override
    public boolean containsQueryType(final String queryType)
    {
        return map.get(queryType) != null;
    }

    @Override
    public QueryAction<?> getQueryAction(final String queryType) throws UnknownQueryTypeException
    {
        return map.get(queryType);
    }

    @Override
    public Collection<String> getQueryTypes()
    {
        return map.keySet();
    }
}
