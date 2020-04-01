package upc.preprocesser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class ParseCSV {

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

        String csvFile = args[0];
        String outputFile=args[1];
        BufferedReader br = null;
        List<String[]> csvIn=new ArrayList<>();
        String[] headers=null;
        Map<String,String> originalRecs=new HashMap<>();
        try {

            FileReader filereader = new FileReader(csvFile);

            // create csvReader object passing
            // file reader as a parameter
            CSVReader csvReader = new CSVReader(filereader);
            headers=csvReader.readNext();
           /* while ((nextRecord = csvReader.readNext()) != null) {
                csvIn.add(nextRecord);
            }*/
           csvIn=csvReader.readAll();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Map<String,List<String>> reqKeywords=new HashMap<>();
        Map<String,String> reqId=new HashMap<>();
        int req1id = 0,rec1 = 0,rec2 = 0,rec2id = 0;
        for (int i=0;i<headers.length;++i) {
            if (headers[i].equals("req1")) {
                rec1=i;
            }
            else if(headers[i].equals("req1_id")) {
                req1id=i;
            }
            if (headers[i].equals("req2")) {
                rec2=i;
            }
            else if(headers[i].equals("req2_id")) {
                rec2id=i;
            }
        }
        for (String[] row:csvIn) {
            if (row.length<=rec1||row.length<=req1id||row.length<=rec2||row.length<=rec2id) break;
            String req1=row[rec1];
            String req1_id=row[req1id];
            String req2=row[rec2];
            String req2_id=row[rec2id];
            if (!reqId.containsKey(req1_id)) reqId.put(req1_id,req1);
            if (!reqId.containsKey(req2_id)) reqId.put(req2_id,req2);
            if (!originalRecs.containsKey(req1_id)) originalRecs.put(req1_id,req1);
            if (!originalRecs.containsKey(req2_id)) originalRecs.put(req2_id,req2);

        }
        List<String> ids=new ArrayList<>();
        List<String> vals=new ArrayList<>();
        for (String s:reqId.keySet()) {
            ids.add(s);
            vals.add(reqId.get(s));
        }
        TFIDFKeywordExtractor cleaner=new TFIDFKeywordExtractor(0.0);
        reqKeywords=cleaner.clean(vals,ids);
        Map<String, Set<String>> ngrams=getNgrams(reqKeywords,cleaner.getWordOrder(),Integer.valueOf(3));
        FileWriter fileWriter = new FileWriter(outputFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        int counter=0;
        String[] newHeaders=new String[headers.length+2];
        for (int i=0;i<headers.length;++i) {
            newHeaders[i]=headers[i];
        }

        Writer writer = Files.newBufferedWriter(Paths.get(args[1]));

        CSVWriter csvWriter = new CSVWriter(writer,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);



        newHeaders[newHeaders.length-2]="original_req1";
        newHeaders[newHeaders.length-1]="original_req2";
        String joined = String.join(",", newHeaders);
        csvWriter.writeNext(newHeaders);
        Integer req1pos=newHeaders.length-2;
        Integer req2pos=newHeaders.length-1;
        Integer size=newHeaders.length;

        for (String[] row:csvIn) {
            String[] newRow=new String[size];
            for (int i=0;i<row.length;++i) {
                newRow[i]=row[i];
            }
            if (row.length<=rec1||row.length<=req1id||row.length<=rec2||row.length<=rec2id) break;
            String req1_id=newRow[req1id];
            String req2_id=newRow[rec2id];
            List<String> preprocessedReq1=new ArrayList<>(ngrams.get(req1_id));
            List<String> preprocessedReq2=new ArrayList<>(ngrams.get(req2_id));
            String trueReq1="[";
            String trueReq2="[";
            if (preprocessedReq1.size()==0) counter++;
            if (preprocessedReq2.size()==0) counter++;
            for (int i=0;i<preprocessedReq1.size();++i) {
                if (i==0) {
                    trueReq1+="'"+preprocessedReq1.get(i)+"'";
                }
                else {
                    trueReq1+=" , '"+preprocessedReq1.get(i)+"'";
                }
            }
            for (int i=0;i<preprocessedReq2.size();++i) {
                if (i==0) {
                    trueReq2+="'"+preprocessedReq2.get(i)+"'";
                }
                else {
                    trueReq2+=" , '"+preprocessedReq2.get(i)+"'";
                }
            }
             trueReq1+="]";
             trueReq2+="]";
            newRow[rec1]=trueReq1;
            newRow[rec2]=trueReq2;
            String auxOriginalRec=originalRecs.get(req1_id);
            auxOriginalRec=auxOriginalRec.replace(',',' ');
            newRow[req1pos]=auxOriginalRec;
            auxOriginalRec=originalRecs.get(req2_id);
            auxOriginalRec=auxOriginalRec.replace(',',' ');
            newRow[req2pos]=auxOriginalRec;
            //String finalRow = String.join(",", newRow);
            if (preprocessedReq1.size()>0&&preprocessedReq2.size()>0)csvWriter.writeNext(newRow);
            printWriter.flush();
        }

    }


    private static Map<String, Set<String>> getNgrams(Map<String, List<String>> keywords, Map<String, Map<String, List<Integer>>> wordOrder, int maxSize) {
        Map<String,Set<String>> result=new HashMap<>();
        for (String s:keywords.keySet()) {
            TreeMap<Integer,String> orderedKeywords=new TreeMap<>();
            for (String k:keywords.get(s)) {
                if (wordOrder.get(s).containsKey(k)) {
                    for (Integer i : wordOrder.get(s).get(k)) {
                        orderedKeywords.put(i, k);
                    }
                }
            }
            List<String> ordered=new ArrayList<>();
            for (String o:orderedKeywords.values()) {
                ordered.add(o);
            }
            Set<String> ngrams=ngrams(ordered,maxSize);
            result.put(s,ngrams);
        }
        return result;
    }

    private static Set<String> ngrams(List<String> ordered, int maxSize) {
        Set<String> result=new HashSet<>();
        for (int i=0;i<ordered.size();++i) {
            String cumulative="";
            //for (int j=i;j<maxSize && j<ordered.size();++j) {
            for (int j = i; (j-i) < maxSize && j < ordered.size(); ++j) {
                if (cumulative.equals("")) cumulative=ordered.get(j);
                else cumulative=cumulative+" "+ordered.get(j);
                result.add(cumulative);
            }
        }
        return result;
    }

}
