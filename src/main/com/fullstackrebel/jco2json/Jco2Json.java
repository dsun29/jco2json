package com.fullstackrebel.jco2json;

import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.JCO;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.util.*;

/**
 *  Jco2Json parses JSON string into Map structure, and passes the values to a JCo function as import parameters,
 *  then convert exmport parameters (including tables) into a JSON String
 *
 *  Homepage: https://github.com/dsun29/jso2json
 *  @author Dayong Sun <sundavy@gmail.com>
 *
 */

public class Jco2Json
{
    JCO.Client mConnection;
    JCO.Repository mRepository;

    private JCO.Function function;
    private org.json.simple.parser.JSONParser parser;
    private org.json.simple.parser.ContainerFactory containerFactory;



    public Jco2Json(String host, String systemNumber, String client, String userid, String password,  String repo) throws Exception{
        try {
            // Change the logon information to your own system/user
            mConnection = JCO.createClient(client, // SAP client
                    userid, // userid
                    password, // password
                    "EN", // language
                    host, // application server host name
                    systemNumber); // system number

            mConnection.connect();
            mRepository = new JCO.Repository(repo, mConnection);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw ex;

        }
    }

    public void setFunction(String functionName) throws Exception{
        try {
            IFunctionTemplate ft = mRepository.getFunctionTemplate(functionName);
            if (ft == null)
                throw new Exception("Problem retrieving JCO.Function object");

            this.function = ft.getFunction();
        }
        catch (Exception ex) {
            throw ex;
        }

    }


    private void createJsonParser()
    {
        this.parser = new org.json.simple.parser.JSONParser();
        containerFactory = new org.json.simple.parser.ContainerFactory()
        {
            @Override
            public List creatArrayContainer()
            {
                return new LinkedList();
            }
            @Override
            public Map createObjectContainer()
            {
                return new LinkedHashMap();
            }
        };
    }
    /**
     *
     * @return json Representation of the JCo Function call
     * @throws Exception
     */
    public String execute() throws Exception
    {
        LinkedHashMap resultList = new LinkedHashMap();
        try
        {
            mConnection.execute(function);

        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }

        //Export Parameters
        if(function.getExportParameterList()!= null)
        {
            getFields(function.getExportParameterList(),resultList);
        }

        //Table Parameters
        if(function.getTableParameterList()!= null)
        {
            getFields(function.getTableParameterList(),resultList);
        }

        try {
            if (mConnection != null) {
                mConnection.disconnect();
            }
        }
        catch(Exception e){

        }

        return JSONValue.toJSONString(resultList);
    }

    private void getFields(JCO.ParameterList parameterList, LinkedHashMap resultList)
    {
        int length = parameterList.getNumFields();
        for(int i=0; i<length; i++) {
            JCO.Field field = parameterList.getField(i);
            LinkedHashMap map = new LinkedHashMap();
            map.put(field.getName(), field.getValue());

            if(field.isTable()) {
                resultList.put(field.getName(),getTableParameter(parameterList.getTable(i)));
            }
            else if (field.isStructure())
            {
                resultList.put(field.getName(),getStructureParameter(parameterList.getStructure(i)));
            }
            else
            {

                resultList.put(field.getName(),field.getValue().toString());
            }
        }
    }
    private LinkedList getTableParameter(JCO.Table table)
    {
        LinkedList l = new LinkedList();
        if(table.getNumRows() < 1){
            return l;
        }
        table.firstRow();
        do{

            LinkedHashMap m = new LinkedHashMap();
            int numColumns = table.getNumColumns();
            for(int i=0; i<numColumns; i++) {
                JCO.Field field = table.getField(i);
                Object value =  table.getValue(field.getName());
                if (value == null) value = "";
                String str_value = value.toString();
                m.put(field.getName(), str_value);
            }
            l.add(m);
        } while(table.nextRow());

        return l;
    }
    private LinkedHashMap getStructureParameter(JCO.Structure structure)
    {
        LinkedHashMap m = new LinkedHashMap();
        int length = structure.getNumFields();
        for(int i=0; i<length; i++) {
            JCO.Field field = structure.getField(i);
            Object value = field.getValue();
            if(value == null) value = "";
            m.put(field.getName(), value.toString());
        }

        return m;
    }
    /**
     *  Sets the functions parameters from a json String
     * @param jsonParameters a json representation of the function parameters
     * @throws ParseException
     */
    public void setParameters(String jsonParameters) throws ParseException
    {
        createJsonParser();
        Map params = (Map)parser.parse(jsonParameters, containerFactory);
        setParameters(params);
    }
    /**
     * Sets the functions parameters from a Map
     * @param Parameters a Map of parameters of type LinkedHashMap or LinkedList)
     *
     */
    public void setParameters(Map Parameters)
    {
        Iterator iter = Parameters.entrySet().iterator();
        while(iter.hasNext())
        {
            Map.Entry parameter = (Map.Entry)iter.next();
            setParameter(parameter.getKey().toString(),parameter.getValue());
        }
    }
    /**
     * Sets a single parameter
     * @param name the name of the parameter
     * @param value the value of the parameter (String, Integer, LinkedHashMap or LinkedList)
     */
    public void setParameter(String name, Object value)
    {
        if(value instanceof LinkedList)
        {
            setTableParameter(name,(LinkedList)value);
        }
        else if (value instanceof LinkedHashMap)
        {
            setStructureParameter(name,(LinkedHashMap)value);
        }
        else
        {
            setSimpleParameter(name,value);
        }
    }
    /**
     * Sets a single Importing or Changing parameter that is not a structure
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setSimpleParameter(String name, Object value)
    {
        //Find Simple, non structure or table parameter with this name and set the appropriate value
        //Importing Parameters
        if(function.getImportParameterList()!= null)
        {
            setSimpleParameterValue(function.getImportParameterList(),name, value);
        }

    }
    private void setSimpleParameterValue(JCO.ParameterList paramList, String name, Object value)
    {

        int length = paramList.getNumFields();
        for(int i=0; i<length; i++) {
            String fieldName = paramList.getName(i);
            JCO.Field field = paramList.getField(i);
            if(fieldName.equals(name) && !field.isStructure() && !field.isTable()) {
                field.setValue(value);
            }
        }

    }


    /**
     * Sets a single Importing or Changing parameter that is a structure
     * @param name the name of the parameter
     * @param map the value of the parameter
     */
    public void setStructureParameter(String name, LinkedHashMap map)
    {
        //Find structure parameter with this name and set the appropriate values
        JCO.Structure structureParams = function.getImportParameterList().getStructure(name);
        if(structureParams == null) return;

        Iterator fieldIter = map.entrySet().iterator();
        while(fieldIter.hasNext())
        {
            Map.Entry singleField = (Map.Entry)fieldIter.next();
            //In JCO2, setValue method takes value first, then field name
            structureParams.setValue(singleField.getValue().toString(), singleField.getKey().toString());
        }

    }
    /**
     *  Sets a single Table parameter that is a structure
     * @param name the name of the parameter
     * @param list The value of the parameter (A LinkedList of LinkedHashmaps)
     */
    public void setTableParameter(String name, LinkedList list)
    {
        //Find table parameter with this name and set the appropriate valies
        int numTabls = function.getTableParameterList().getNumFields();
        for(int i=0; i<numTabls; i++) {
            JCO.Table table = function.getTableParameterList().getTable(i);
            String tableName = table.getName();
            if (tableName.equals(name)) {
                Iterator recordIter = list.listIterator();
                while(recordIter.hasNext())
                {
                    table.appendRow();
                    LinkedHashMap fields = (LinkedHashMap)recordIter.next();
                    Iterator fieldIter = fields.entrySet().iterator();
                    while(fieldIter.hasNext())
                    {
                        Map.Entry field = (Map.Entry)fieldIter.next();
                        table.setValue(field.getValue().toString(), field.getKey().toString());
                    }
                }
            }

        }
    }


    //Example
    public static void main(String[] args){
        try{
            Jco2Json jco = new Jco2Json("dev.fullstackrebel.com", "01", "100", "dsun29", "123123", "testing");

            jco.setFunction("BAPI_BANK_GETDETAIL");
            jco.setParameters("{\"BANKKEY\":\"083000108\",\"BANKCOUNTRY\":\"US\"}");
            String results = jco.execute();
            System.out.println(results);

        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
    }


}


