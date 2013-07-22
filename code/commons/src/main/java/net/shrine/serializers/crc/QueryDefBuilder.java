package net.shrine.serializers.crc;

import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainDateType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import org.spin.tools.NetworkTime;
import org.spin.tools.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
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
 *         Date: Jun 1, 2010
 *         Time: 11:22:02 AM
 */
public class QueryDefBuilder
{
    public static String SHRINE = "SHRINE";

    //Categories
    public static String Demographics = "Demographics";
    public static String Diagnosis = "Diagnosis";
    public static String Medications = "Medications";
    public static String Labs = "Labs";

    //Demographic Frequent Items
    public static String Age = "Age";
    public static String Gender = "Gender";
    public static String Male = "Male";
    public static String Female = "Female";

    public static String PATH_DEMOGRAPHICS = buildPathWithPrefix(SHRINE, Demographics);
    public static String PATH_DIAGNOSIS = buildPathWithPrefix(SHRINE, Diagnosis);
    public static String PATH_MEDICATIONS = buildPathWithPrefix(SHRINE, Medications);
    public static String PATH_LABS = buildPathWithPrefix(SHRINE, Labs);

    public static String PATH_MALE = buildPathWithPrefix(SHRINE, Demographics, Gender, Male);
    public static String PATH_FEMALE = buildPathWithPrefix(SHRINE, Demographics, Gender, Female);

    public static QueryDefinitionType getQueryDefinition(List<PanelType> panels)
    {
        QueryDefinitionType queryDef = new QueryDefinitionType();

        for(int p = 0; p < panels.size(); p++)
        {
            PanelType panel = panels.get(p);
            panel.setPanelNumber(p + 1);

            queryDef.getPanel().add(panel);
        }

        return queryDef;
    }

    public static QueryDefinitionType getQueryDefinition(PanelType... panels)
    {
        return getQueryDefinition(Arrays.asList(panels));
    }

    public static QueryDefinitionType getQueryDefinition(ItemType... itemsInSinglePanel)
    {
        return getQueryDefinition(getPanel(itemsInSinglePanel));
    }

    public static QueryDefinitionType getQueryDefinition(String... itemKeysSinglePanel)
    {
        return getQueryDefinition(getPanel(itemKeysSinglePanel));
    }

    public static PanelType getPanel(List<ItemType> items)
    {
        PanelType panel = new PanelType();

        //DEFAULTS
        panel.setPanelNumber(1);
        panel.setInvert(0);

        PanelType.TotalItemOccurrences occurences = new PanelType.TotalItemOccurrences();
        occurences.setValue(items.size());

        panel.setTotalItemOccurrences(occurences);

        for(ItemType item : items)
        {
            panel.getItem().add(item);
        }

        return panel;
    }

    public static PanelType getPanel(ItemType... items)
    {
        return getPanel(Arrays.asList(items));
    }

    public static ConstrainDateType getDateConstraint(int year)
    {
        ConstrainDateType constraint = new ConstrainDateType();

        GregorianCalendar calendar = new GregorianCalendar();
                    calendar.set(year, 1, 1);

        constraint.setValue(NetworkTime.makeXMLGregorianCalendar(calendar));

        return constraint;
    }

    public static PanelType getPanel(int yearStart, int yearEnd, ItemType ... items)
    {
        PanelType panel = getPanel(items);

        panel.setPanelDateFrom(getDateConstraint(yearStart));
        panel.setPanelDateTo(getDateConstraint(yearEnd));

        return panel;
    }

    public static PanelType getPanel(String... itemKeys)
    {
        return getPanel(getItems(itemKeys));
    }

    public static ItemType getItemGenderMale()
    {
        return getItem(PATH_MALE);
    }

    public static ItemType getItemGenderFemale()
    {
        return getItem(PATH_FEMALE);
    }

    public static ItemType getItem(String path)
    {
        ItemType item = new ItemType();
        //
        item.setItemKey(path);
        //
        return item;
    }

    public static ItemType[] getItems(List<String> itemKeys)
    {
        ItemType[] items = new ItemType[itemKeys.size()];

        for(int i = 0; i < itemKeys.size(); i++)
        {
            items[i] = getItem(itemKeys.get(i));
        }

        return items;
    }

    public static ItemType[] getItems(String... itemKeys)
    {
        return getItems(Arrays.asList(itemKeys));
    }


    public static String SLASH = "\\";

    public static String getPathPrefix(String PREFIX)
    {
        return SLASH + SLASH + PREFIX + SLASH;
    }

    public static String buildPath(String... traversal)
    {
        StringBuilder path = new StringBuilder();

        for(String node : traversal)
        {
            path.append(node);
            path.append(SLASH);
        }

        return path.toString();
    }

    //TODO: refactor, silly \\SHRINE\SHRINE
    public static String buildPathWithPrefix(String PREFIX, String... traversal)
    {
        return getPathPrefix(PREFIX) + buildPath(traversal);
    }

    public static ItemType getItemAge(int yearsOld)
    {
        String ageBin    = getAgeBin(yearsOld);
        String ageString = yearsOld + I2B2_YEARS_OLD_STRING;

        if(between(yearsOld, 0, 89))
        {
            return getItem(buildPathWithPrefix(SHRINE, SHRINE, Demographics, Age, ageBin, ageString));
        }
        else
        {
            return getItemAgeBin(ageBin);
        }
    }

    public static ItemType getItemAgeBin(String ageRange)
    {
        return getItem(buildPathWithPrefix(SHRINE, SHRINE, Demographics, Age, ageRange));
    }

    public static ArrayList<String> getAgeBins()
    {
        ArrayList<String> ranges = Util.makeArrayList();
        

        for(int i=0; i < 100; i++)
        {
            String bin = getAgeBin(i);

            if(!ranges.contains(bin)) ranges.add(bin);
        }

        return ranges;
    }


    public static String getAgeBin(int yearsOld)
    {
        if(yearsOld >= 90)
        {
            return ">=90"+I2B2_YEARS_OLD_STRING;
        }
        else
        {
            String age = null;

                    if(between(yearsOld, 0,  9))    age = "0-9";
            else    if(between(yearsOld, 10, 17))   age = "10-17";
            else    if(between(yearsOld, 18, 34))   age = "18-34";
            else    if(between(yearsOld, 35, 44))   age = "35-44";
            else    if(between(yearsOld, 45, 54))   age = "45-54";
            else    if(between(yearsOld, 55, 64))   age = "55-64";
            else    if(between(yearsOld, 65, 74))   age = "65-74";
            else    if(between(yearsOld, 75, 84))   age = "75-84";
            else    if(between(yearsOld, 85, 89))   age = "85-89";

            return age + I2B2_YEARS_OLD_STRING;
        }
    }

    private static boolean between(int number, int min, int max)
    {
        return (number >= min && number <= max);
    }

    private static String I2B2_YEARS_OLD_STRING = " years old";
}
