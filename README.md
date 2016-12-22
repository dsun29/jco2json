#Jco2Json - JSON API for SAP JCo2

This class allows to pass parameters to RFCs or BAPIs in JSON string, and then retrieves return parameters (including exporting parameters and table parameters) in JSON string.

**Note** Please refer to [jcoson](https://github.com/dsun29/jcoSon) if you are looking for JSON API for **JCo3**.

##Prerequisite
- JDK 1.7+
- SAP JCo2

##Example
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

##Disclaimer
This project is inspired by the [jcoson](https://github.com/dhorions/jcoSon) project for JCo3, and majority of the code is copied from the same project.


##Changelog
###1.0.0 - 12-22-2016
**First released**

#License
MIT License

Copyright (c) [year] [fullname]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.