package net.shrine.utilities.query;

import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import net.shrine.serializers.crc.QueryDefBuilder;
import org.spin.tools.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

/**
 * @author Andrew McMurry, MS
 *         <p/>
 *         With primary support from Children's Hospital Informatics Program @
 *         Harvard-MIT Health Sciences and Technology and
 *         <p/>
 *         Secondary support from the Harvard Medical School
 *         Center for BioMedical Informatics
 *         <p/>
 *         PHD candidate, Boston University Bioinformatics
 *         Member, I2b2 National Center for Biomedical Computing
 *         <p/>
 *         All works licensed under LGPL
 *         <p/>
 *         User: andy
 *         Date: Oct 19, 2010
 *         Time: 11:04:15 AM
 */ 
public class FlatFileIterator extends QueryDefIterator
{
    public static String COMMENT_LINE  = "#";

    File            file;
    BufferedReader  reader;
    
    ShortHandNotation current  = null;

    public FlatFileIterator(File file) throws FileNotFoundException
    {
        this.file   = file;
        this.reader =  new BufferedReader(new FileReader(file));
    }

    @Override
    public boolean hasNext()
    {
        try
        {
            return reader.ready() && current!= ShortHandNotation.end;
        }
        catch(Exception e)
        {
            log.error("Could not determine if Flat File has more Query Items", e);
        }

        return false;
    }

    @Override
    public QueryDefinitionType next()
    {
        QueryDefinitionType definition  = null;  
        List<ItemType>      itemKeys    = Util.makeArrayList();
        List<PanelType>     panels      = Util.makeArrayList();

        try
        {
            while(hasNext() && definition==null)
            {
                String lineNext = reader.readLine();

                log.debug("CURRENT:"+ lineNext);

                //Comment Line
                if(lineNext.startsWith(COMMENT_LINE))
                {
                    System.out.println(COMMENT_LINE);
                }
                else
                {
                    //Annotated Line
                    if(ShortHandNotation.isNotation(lineNext))
                    {
                        current  = ShortHandNotation.parse(lineNext);

                        if(itemKeys.size() > 0)
                        {
                            panels.add(QueryDefBuilder.getPanel(itemKeys));

                            itemKeys.clear();
                        }

                        if(current!= ShortHandNotation.panel)
                        {
                            if(panels.size() >  0)
                            {
                                log.debug("Returning buffer of panels within query definition.");
                                definition= QueryDefBuilder.getQueryDefinition(panels);

                                panels.clear();
                            }
                        }
                    }
                    //Item Key line 
                    else if(lineNext.length() > 0)
                    {
                        switch (current)
                        {
                            case name:
                                        definition.setQueryName(lineNext);
                                        break;

                            case panel:
                                        itemKeys.add(QueryDefBuilder.getItem(lineNext));
                                        break;

                            default:
                                log.debug("Annotation not yet supported: "+current.print()); 
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            log.error("Could not read next query definition", e);
        }

        return definition;
    }
}
