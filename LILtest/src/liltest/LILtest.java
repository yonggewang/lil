/************************************************************************************************************************************************/
/*                                                                                                                                              */
/* Copyright 2014/2015 Yongge Wang all Rights Reserved.                                                                               */
/* Permission to use, copy, modify, and distribute this software and its documentation for educational, research, and not-for-profit purposes,  */
/* without fee and without a signed licensing agreement, is hereby granted, provided that the above copyright notice, and this paragraph        */
/* appear in all copies, modifications, and distributions.                                                                                      */
/*                                                                                                                                              */
/************************************************************************ƒ************************************************************************/


package liltest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.StrictMath.sqrt;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import rcaller.RCaller;


/**
 *
 * @author yonggewang
 */
public class LILtest {

    /**
     * @param args the command line arguments
     */
    private static String  versionNum = "lilTEST version 0.1"; // 
    static byte[] gRandBuf=null; // Make buffer global to class to keep it off the stack
  
    public static void main(String[] args) throws IOException
  {
    int aleph = 0;
    boolean invalidSwitch = false;        // True if there is a command line switch parse error
    boolean snapRequested = false;         //plot the snaptShote curve
    boolean helpRequested = false;        // Set if -h detected
    boolean plotRequested = false;
    boolean singleFile = false;            //random bits are contained in a single file
    boolean lilFileRequested = true;
    boolean versionNumRequested = false;   // User asked for app version number?
    String testResults = "LILtestResults.txt"; //contains the final testing results
    String plotFilename= "lilCurve.pdf";   // path and filename of the .pdf plot file
    String snapFilename= "lilSnap.pdf";   // path and filename of the .pdf plot file
    String rScriptLocation="/usr/bin/Rscript"; // path of RScript package for plotting. Default is currently C:/R/bin/Rscript.exe
    String arg;                            // Single argument extracted from list
    // If the args are coming in from a file, the command line will contain only: -f filename
    // Read the args from the file and overwrite the args array so args are in here whether from file oc command line
    if(args.length == 2 && args[0].equalsIgnoreCase("-f"))
    {
      // Create a file. Read the args from a text file into string then split into string array reusing args array
      File argsFile = new File(args[1]); // -f filename so gets filename
      args = FileUtils.readFileToString(argsFile).split("\\s+"); // Read args from file into array of args
    }

    // Parse the command line switches. Note: args holds all switches whether from command line or file
    if (args.length == 0) helpRequested = true; // Skip parsing as they have passed nothing in so provide help
    else
    {
      for(int i = 0; i < args.length; i++)
      {
        arg = args[i].toLowerCase();

        if ((i == 0) && arg.charAt(0) != '-')  invalidSwitch = true;
        else if (arg.equals("-h")) helpRequested = true;
        else if (arg.equals("-v")) versionNumRequested = true;
        else if (arg.equals("-p")) //flag for geneerating LIL curves
        {
            plotRequested = true;
            if (i+1 <= args.length-1 && (args[i+1].charAt(0) != '-')) 
                plotFilename = args[i+1]; 
            else
                plotFilename="lilCurve.pdf";  
        }
        else if (arg.equals("-b")) //flag for one large random-bit file
        {
            
            if (i+1 <= args.length-1 && (args[i+1].charAt(0) != '-')) 
            {
                singleFile = true;
                snapRequested = true;     
                plotRequested = true;
                lilFileRequested = false;
            }
            else
                plotFilename="lilCurve.pdf";  
        }
        else if (arg.equals("-s")) 
        {
            snapRequested = true;
            if (i + 1 <= args.length - 1 && (args[i + 1].charAt(0) != '-')) {
                snapFilename = args[i + 1];
            } else {
                snapFilename = "lilSnap.pdf";
            }
        }
        else if (arg.equals("-l")) //random sequence length
            if (i+1 <= args.length-1 && (args[i+1].charAt(0) != '-'))
            {
                String lengthString = args[i+1];
                aleph = Integer.parseInt(lengthString);
                if ((aleph >15)||(aleph <0))
                {
                    invalidSwitch = true;
                }
            }
            else 
                invalidSwitch = true;   
        
        else if (arg.equals("-n")) lilFileRequested = false; //not generate LIL files
        else if (arg.equals("-r")) //Rscript path
            if (i+1 <= args.length-1 && (args[i+1].charAt(0) != '-')) // 
               rScriptLocation = args[i+1];  
              else
               invalidSwitch = true;     
      } // end for processing args
    } // end if args == 0

    if (versionNumRequested)
    {
      System.out.println(versionNum);
      System.out.flush();
    }
    
    if (helpRequested) 
    {
       showHelp();
       System.out.flush();
       System.exit(1); // Still exit with error as no processing has taken place
    }

    System.out.println("***** lil testing process started *****");
    if (invalidSwitch) 
    {
      System.out.println("Invalid switch in command line: " + java.util.Arrays.toString(args) + ".\nApplication will terminate. Use -h for help.") ;
      System.exit(1);
    }
    
    FileWriter resultFile = null;
    resultFile = new FileWriter(testResults, false); // Create output file for LIL data for plotting graph
    
    resultFile.write("The following is a list of the testing results:" + System.getProperty("line.separator"));

    if (singleFile){
        System.out.println("Begin to divide the random files into sequences");
        
    }
    
            
    if (lilFileRequested){
        System.out.println("Begin to calculate LIL values");
        File folder = new File(".");
        String currentFileName = "";
        String curFileExt = "";
        File[] listOfFiles = folder.listFiles();
        int numRandF = 0;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                currentFileName = file.getName();
                curFileExt = FilenameUtils.getExtension(currentFileName);
                if ("rnd".equals(curFileExt)) {
                    numRandF++;

                }

            }
        }
        resultFile.write("You have requested to generate the LIL statistics for each of your \n"
                + "random sequences. I found "+numRandF +" sequences (each has extension .rnd)\n "
                + "and have generated lil file for each of these random sequences."
                + System.getProperty("line.separator"));
    }

            

    
    if (plotRequested) // require a plot of the LIL file so call displayinR to create a pdf file
    {
        System.out.println("Begin to create LIL curve file:"+plotFilename);
        displayinR(plotFilename, rScriptLocation); // If displayGraph is true then show it else don't
        System.out.println(plotFilename+" created.");
        resultFile.write("\nYou have requested to generate the LIL curve pdf file. "
                + "The resulting \nLIL curve file could be found in: " +plotFilename 
                + System.getProperty("line.separator"));
    } 
    else System.out.println("No plot file requested");
    
    if (snapRequested) // plot the snaptShot Normal distribuiton curve
    {
        System.out.println("Create snapShot file:"+ snapFilename);
        snapCurve(snapFilename, rScriptLocation, aleph,resultFile); // If displayGraph is true then show it else don't
        System.out.println(snapFilename+" created.");
    } 
    else System.out.println("No snapShot testing requested");
    
    resultFile.write("Thanks for using LIL testing tools. Please note that the obtained results\n"
           + "and the software is for education/research use only. The technologies contained\n"
           + "in this software are protected under the US Patent Publication No. US0199175-A1\n"
           + "This software or the testing results should not be used for any commercial purpose.\n"
           + "For details, check\n"
            + "http://www.sciencedirect.com/science/article/pii/S0167404815000693\n"
            + "http://webpages.uncc.edu/yonwang/liltest/\n"
            + "or contact: Yongge.Wang@gmail.com\n");
    resultFile.close();
    
    BufferedReader br = new BufferedReader(new FileReader(testResults));
    String line = null;
    while ((line = br.readLine()) != null) {
        System.out.println(line);
    }
    br.close();
      
  } // end main
    
  
    
  // Application on-screen command line syntax and general help from a text file which can be opened independantly if required
  //private static void showHelp(String helpFile) throws IOException 
  private static void showHelp() throws IOException         
  {
   // Since simplifying the command line interface, the help is simpler so no longer requires a file to hold it but left code
   // in case that changes in the future.
   /* try
    {
      BufferedReader br = new BufferedReader(new FileReader(helpFile));
      String line = null;
      while ((line = br.readLine()) != null) 
      {
        System.out.println(line);
      }
    }
    catch(IOException ex)
    {
      System.out.println("Missing file is " + helpFile + ". Application will terminate.");  
    }
    finally
    {
      System.out.flush(); // Need to flush output stream to get text on screen
    }
    */
/*
      String to = "bankofworld@gmail.com";
      String from = "web@gmail.com";
      String host = "localhost";
      Properties properties = System.getProperties();
      properties.setProperty("mail.smtp.host", host);
      Session session = Session.getDefaultInstance(properties);
      Properties props = new Properties();
      props.put("mail.smtp.host", "my-mail-server");
      props.put("mail.from", "me@example.com");
      Session session = Session.getInstance(props, null);
      String mailbody="LIL testing log file";
      InetAddress IP=InetAddress.getLocalHost();
      mailbody=mailbody + IP.toString();
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setSubject("LIL testing runs!");
      message.setText(mailbody);
      Transport.send(message);
      */
     

    
      
   System.out.println("\n\n********************************************** LILtest help ***********************************************");
   System.out.println("This software is for education/research purpose only and should not be used \n"
           + "for any commercial purpose. The technology contained in this software package\n"
           + "is protected under the US Patent Publicaiton No. US-2015-0199175-A1\n"
           + "For details, check\n"
            + "http://www.sciencedirect.com/science/article/pii/S0167404815000693\n"
            + "http://webpages.uncc.edu/yonwang/liltest/\n"
            + "or contact: Yongge.Wang@gmail.com\n\n"
           + "*****************************************************************************************\n"
           + "For LIL testing, you do not need to install the third party software \"The R Project\n"
           + "for Statistical Computing\" which is available at https://www.r-project.org\n"
           + "However, if you want to generate the PDF files for the LIL curve (to show the effect\n"
           + "of Brownian motions of your random sequence curves) or the snapShot normal distribution\n"
           + "curves, you should install the software R first and then hand Rscript path via flag -r.\n"
           + "In order to run this Java code, please copy the randome sequence file\n"
           + "to the directory that contains this LILtest.jar file. You have two choices to input\n"
           + "your random bits to this testing software. The first choice is that you have a large\n"
           + "single file that contains the binary random bits. In this case, please just run the\n"
           + "following commands: \n\n"
           + "java -jar LILtest.jar -b randombit -l <num>\n\n"
           + "where \'randombit\' is the file name that contains your random bits and <num> should\n"
           + "be a number from 0 to 15. The <num> means that randombit file will be divided into \n"
           + "sequences of 2^<num>*64KB length. In order to be accurate in LIL testing, it is recommended"
           + "that you get at least 1000 sequqneces. That is, your randombit should be at least \n"
           + "1000*2^<num>*64KB. For example, if you take <num>=0, then the randombit should be at least\n"
           + "62.5MB. If you take <num>=1, the randombit should be at least 125MB.\n\n"
           + "The second choice is that you have many random bit files (most users will not use this \n"
           + "option though). Each random sequence should be a binary file with the extension .rnd.n"
           + "For example, you could have random sequence files: sequen0001.rnd, ..., sequen9999.rnd\n"
           + "in this directory for processing.\n\n"
           + "Currently, this software supports random sequences of \n"
           + "64KB (default), 128KB (use flag \'-l 1\'), ..., 2GB (use flag \'-l 15\')\n"
           + "That is, each of your sequence should be of size 64KB to 2GB and ll sequenes must \n"
           + "have the same length.\n"
           + "*****************************************************************************************\n\n"
           + "Note: switches can be in any order and are not case sensitive. Paths and filenames must not contain spaces");
   System.out.println("-h  Provides help                     Overrides any other switches to simply provide this help");
   System.out.println("-v  Provides the version number");
   System.out.println("-n  not create LIL data               If you have LIL data already, please use this flag");
   System.out.println("-r  path_to_Rscript                   This is the path to the Rscript");
   System.out.println("-l  length of each random string      take values 0,1,2,3,4,5,6,7,8");
   System.out.println("-p  Create plot image                 Plot the LIL data in a PDF file");
   System.out.println("-s  Create snap Testing image         Plot the LIL snap Testing in a PDF file");
   System.out.println("-b  Only one large random bit file    this will overwrite all other flags");
   System.out.println("-f  File name                         The command line args can be read from a file");
   System.out.println("\nIf -r flag is not set, the default Rscript path is /usr/bin/Rscript. this is true for MacOS and Linux\n");
   System.out.println("Example: java -jar LILtest.jar -r /usr/bin/Rscript -l 7 -p -s");
   System.out.println("Example: java -jar LILtest.jar -p lilCurve.pdf -s lilSnap.pdf");
   System.out.println("Example: java -jar LILtest.jar -f args.txt\n\n"
           + "If you are not sure about these flags and have many separate random sequence files, just run:\n"
           + "Example: java -jar LILtest.jar -p -s -l 0\n\n"
           + "If you only have one large random bit file, just run:\n"
           + "Example: java -jar LILtest.jar -b yourRandomfileName -l <0..15>\n\n"
           + "The testing reult is contained in the file LILtestResults.txt");
   System.out.println("***********************************************************************************************************");
}
  
  /***************************************************************************************************************************************/
  /*                                                                                                                                     */
  /* Third the "R" party statistical analysis application to create the LIL data graph and save as an image. To remote-access the        */
  /* application, RCaller 2.1.1 is used so the application and JAva files need to be installed and the path to RScript is passed in      */
  /* to the application via the command line which passes it on to this function.                                                        */
  /* Refer to the "R" documentation to understand the syntax of the "R" commands.                                                        */
  /*                                                                                                                                     */
  /* plotFilename:  the file name to be created                                                                            */ 
  /* rScriptPath:  the path to the RScript executable                                                                                    */
  /*                                                                                                                                     */
  /***************************************************************************************************************************************/
  
  private static void displayinR(String plotFilename, String rScriptPath) throws IOException 
  {
    // Rcaller from: http://code.google.com/p/rcaller/downloads/list
    //example: http://stdioe.blogspot.com/2011/07/rcaller-20-calling-r-from-java.html

    try 
    {
      RCaller caller = new RCaller(); 
      caller.setRscriptExecutable(rScriptPath); 
      caller.cleanRCode();
      rcaller.RCode code = new rcaller.RCode();
      caller.setRCode(code);   
      code.clear();
      code.addRCode("pdf(\'"+plotFilename+"\', height=3.5, width=5)");
      //code.addRCode("jpeg(\'lilCurve.jpg\')");
      File folder = new File(".");
      String currentFileName = "";
      String curFileExt = "";
      File[] listOfFiles = folder.listFiles();
      String lilFilename = "";
      boolean firstplot = true;
      int counter =0;
      for (File lfile : listOfFiles) {
          if (lfile.isFile()) {
              currentFileName = lfile.getName();
              curFileExt = FilenameUtils.getExtension(currentFileName);
              if ("lil".equals(curFileExt)) {
                  lilFilename = FilenameUtils.removeExtension(currentFileName) + ".lil";
                  lilFilename = lilFilename.replace('\\', '/'); // Rcaller doesn't like \\
                  code.addRCode("file<-\'" + lilFilename + "\'");
                  code.addRCode("data<-read.table(file, header=T, sep=\'\\t\')");
                  //code.addRCode("data"+counter+"<-read.table(file, header=T)"); // If header is F or a string not on first line of lil data then it plots nothing!
                  if (firstplot) {
                      firstplot = false;
                      //System.out.println("R is plotting graph for: " + lilFilename);
                      code.addRCode("plot(data$LIL, type=\'l\', col=\'black\', ylim=c(-1.5, 1.5), axes=TRUE, ann=FALSE);");
                      counter = counter +1;
                     
                  } else 
                  {
                      code.addRCode("lines(data$LIL, type=\'l\', col=\'black\', pch=23, lty=1);");
                      counter = counter +1;
                  }
              }
          }
      }
      
      code.addRCode("abline(h=0,col=1,lty=3);"); 
      code.addRCode("abline(h=-0.5,col=1,lty=3)\n");
      code.addRCode("abline(h=0.5,col=1,lty=3)\n");
      code.addRCode("abline(h=0.6,col=1,lty=3)\n");
      code.addRCode("abline(h=-0.6,col=1,lty=3)\n");
      code.addRCode("abline(h=0.7,col=1,lty=3)\n");
      code.addRCode("abline(h=-0.7,col=1,lty=3)\n");
      code.addRCode("abline(h=0.8,col=1,lty=3)\n");
      code.addRCode("abline(h=-0.8,col=1,lty=3)\n");
      code.addRCode("abline(h=0.9,col=1,lty=3)\n");
      code.addRCode("abline(h=-0.9,col=1,lty=3)\n");
      code.addRCode("abline(h=1,col=1,lty=3)\n");
      code.addRCode("abline(h=-1,col=1,lty=3)\n");
      code.addRCode("title('Plot of LIL curves')\n"); 
      
      code.addRCode("dev.off()");   
      code.endPlot();
      caller.runOnly();
    } // end displayinR
    catch (Exception e) 
    {
      System.out.println(e.toString());
    }
  }

    private static void snapCurve(String snapFilename, String rScriptPath, int alephVal, FileWriter rFile) {
        try {
            System.out.println("Begin to carry snapShopt LIL testing");
            double[][] snapProb = {
                //snapProb[][i] contains the probability for sequence of length 2^{19+i}
                //snapProb[0][i] is the probability for lil>= 1
                //snapProb[1][i] is the probabiity for 1>lil>=0.95 and so on 
                //Maple code to calculate the probabilities: 
                //int1 := 0.; int2 := 0.5e-1; n := 2^26; sqrt2lnlnn := sqrt(2*ln(ln(n))); evalf(%)
                //distint1TOint2 := (int(exp(-(1/2)*x^2), x = int1*sqrt2lnlnn .. int2*sqrt2lnlnn))/sqrt(2*Pi); evalf(%)  
                {0.01158408240,0.01092030157,0.01032543136,0.009789434555,0.009304117375,0.008862726840,0.008459649035,0.00809, 0.00775, 0.007437, 0.007147, 0.006877, 0.006627, 0.006393, 0.006175, 0.00597},
                {0.003914167128,0.003765149820,0.003628038944,0.003501424238,0.003384113665,0.003275089920,0.003173482114,0.003078536644, 0.00299, 0.002906, 0.002828, 0.002754, 0.002684, 0.002617, 0.002555, 0.002495},
                {0.004999024435,0.004832144086,0.004677761005,0.004534462932,0.004401045463,0.004276476136,0.004159863712,0.004050435438, 0.003948, 0.003851, 0.003759, 0.003672, 0.00359, 0.003512, 0.003438, 0.003368},
                {0.006302884299,0.006120606773,0.005951075453,0.005792919573,0.005644961456,0.005506185395,0.005375708131,0.005252760901, 0.005137, 0.005027, 0.004923, 0.004824, 0.00473, 0.00464, 0.004555, 0.004474},
                {0.007845153662,0.007651488732,0.007470403642,0.007300616598,0.007141020137,0.006990649619,0.006848663668,0.006714321849,  0.006587, 0.006466, 0.006351, 0.006241, 0.006137, 0.006037, 0.005941, 0.00585},
                {0.009639880884,0.009440488340,0.009253030147,0.009076367734,0.008909506536,0.008751573663,0.008601801157,0.008459509218, 0.008324, 0.008195, 0.008072, 0.007954, 0.007841, 0.007733, 0.007629, 0.00753},
                {0.01169364771,0.01149581822,0.01130876256,0.01113153564,0.01096329810,0.01080330908,0.01065090635,0.01050550026, 0.010367, 0.010234, 0.010106, 0.009984, 0.009867, 0.009754, 0.009645, 0.009541},
                {0.01400349479,0.01381599910,0.01363758573,0.01346754695,0.01330524647,0.01315010819,0.01300161149,0.01285928644, 0.012723, 0.012591, 0.012465, 0.012344, 0.012227, 0.012114, 0.012004, 0.011899},
                {0.01655506898,0.01638783837,0.01622748151,0.01607357236,0.01592571040,0.01578351900,0.01564664989,0.01551477751, 0.015388, 0.015265, 0.015146, 0.015032, 0.014921, 0.014813, 0.014709, 0.014608},
                {0.01932117936,0.01918483226,0.01905267665,0.01892460301,0.01880047570,0.01868014714,0.01856346449,0.01845027261, 0.01834, 0.018234, 0.01813, 0.018029, 0.017931, 0.017836, 0.017743, 0.017653},
                {0.02226098563,0.02216620331,0.02207253166,0.02198019808,0.02188935752,0.02180011134,0.02171251998,0.02162661673, 0.021542, 0.02146, 0.021379, 0.0213, 0.021222, 0.021146, 0.021072, 0.020999},
                {0.02531997409,0.02527677024,0.02523129520,0.02518412706,0.02513572220,0.02508644245,0.02503657706,0.02498635661, 0.024936, 0.024886, 0.024835, 0.024785, 0.024735, 0.024686, 0.024637, 0.024588},
                {0.02843087532,0.02844780923,0.02845890581,0.02846509680,0.02846714257,0.02846567008,0.02846119914,0.02845416518, 0.028445, 0.028434, 0.028421, 0.028407, 0.028392, 0.028375, 0.028358, 0.02834},
                {0.03151558142,0.03159897810,0.03167292065,0.03173868252,0.03179732223,0.03184972610,0.03189664231,0.03193870618, 0.031976, 0.03201, 0.032041, 0.032068, 0.032093, 0.032115, 0.032135, 0.032153},
                {0.03448804071,0.03464130316,0.03478157804,0.03491046194,0.03502929387,0.03513920666,0.03524116535,0.03533599770, 0.035424, 0.035507, 0.035584, 0.035657, 0.035725, 0.03579, 0.03585, 0.035907},
                {0.03725802562,0.03748110437,0.03768788073,0.03788024373,0.03805978969,0.03822787583,0.03838566649,0.03853416578, 0.038674, 0.038807, 0.038932, 0.039051, 0.039164, 0.039272, 0.039375, 0.039473},
                {0.03973555307,0.04002464689,0.04029446607,0.04054715679,0.04078453806,0.04100816563,0.04121937807,0.04141933537, 0.041609, 0.041789, 0.041961, 0.042125, 0.042282, 0.042432, 0.042575, 0.042713},
                {0.04183567728,0.04218320874,0.04250894807,0.04281525325,0.04310412954,0.04337729676,0.04363623940,0.04388224706, 0.044116, 0.04434, 0.044553, 0.044758, 0.044953, 0.045141, 0.045322, 0.045496},
                {0.04348329637,0.04387818844,0.04424931717,0.04459920319,0.04492999580,0.04524354118,0.04554143652,0.04582507199, 0.046096, 0.046354, 0.0466, 0.046839, 0.047067, 0.047287, 0.047498, 0.047701},
                {0.04461760251,0.04504584512,0.04544897041,0.04582960929,0.04619000860,0.04653210073,0.04685755944,0.04716784365, 0.047464, 0.047748, 0.04802, 0.048281, 0.048532, 0.048773, 0.049006, 0.04923},
                {0.04519580383,0.04564127471,0.04606093897,0.04645748363,0.04683320461,0.04719007786,0.04752981611,0.04785391292, 0.048164, 0.04846, 0.048745, 0.049018, 0.049281, 0.049534, 0.049778, 0.050013},
                {0.04519580383,0.04564127471,0.04606093897,0.04645748363,0.04683320461,0.04719007786,0.04752981611,0.04785391292, 0.048164, 0.04846, 0.048745, 0.049018, 0.049281, 0.049534, 0.049778, 0.050013},
                {0.04461760251,0.04504584512,0.04544897041,0.04582960929,0.04619000860,0.04653210073,0.04685755944,0.04716784365, 0.047464, 0.047748, 0.04802, 0.048281, 0.048532, 0.048773, 0.049006, 0.04923},
                {0.04348329637,0.04387818844,0.04424931717,0.04459920319,0.04492999580,0.04524354118,0.04554143652,0.04582507199, 0.046096, 0.046354, 0.0466, 0.046839, 0.047067, 0.047287, 0.047498, 0.047701},
                {0.04183567728,0.04218320874,0.04250894807,0.04281525325,0.04310412954,0.04337729676,0.04363623940,0.04388224706, 0.044116, 0.04434, 0.044553, 0.044758, 0.044953, 0.045141, 0.045322, 0.045496},
                {0.03973555307,0.04002464689,0.04029446607,0.04054715679,0.04078453806,0.04100816563,0.04121937807,0.04141933537, 0.041609, 0.041789, 0.041961, 0.042125, 0.042282, 0.042432, 0.042575, 0.042713},
                {0.03725802562,0.03748110437,0.03768788073,0.03788024373,0.03805978969,0.03822787583,0.03838566649,0.03853416578, 0.038674, 0.038807, 0.038932, 0.039051, 0.039164, 0.039272, 0.039375, 0.039473},
                {0.03448804071,0.03464130316,0.03478157804,0.03491046194,0.03502929387,0.03513920666,0.03524116535,0.03533599770, 0.035424, 0.035507, 0.035584, 0.035657, 0.035725, 0.03579, 0.03585, 0.035907},
                {0.03151558142,0.03159897810,0.03167292065,0.03173868252,0.03179732223,0.03184972610,0.03189664231,0.03193870618, 0.031976, 0.03201, 0.032041, 0.032068, 0.032093, 0.032115, 0.032135, 0.032153},
                {0.02843087532,0.02844780923,0.02845890581,0.02846509680,0.02846714257,0.02846567008,0.02846119914,0.02845416518, 0.028445, 0.028434, 0.028421, 0.028407, 0.028392, 0.028375, 0.028358, 0.02834},
                {0.02531997409,0.02527677024,0.02523129520,0.02518412706,0.02513572220,0.02508644245,0.02503657706,0.02498635661, 0.024936, 0.024886, 0.024835, 0.024785, 0.024735, 0.024686, 0.024637, 0.024588},
                {0.02226098563,0.02216620331,0.02207253166,0.02198019808,0.02188935752,0.02180011134,0.02171251998,0.02162661673, 0.021542, 0.02146, 0.021379, 0.0213, 0.021222, 0.021146, 0.021072, 0.020999},
                {0.01932117936,0.01918483226,0.01905267665,0.01892460301,0.01880047570,0.01868014714,0.01856346449,0.01845027261, 0.01834, 0.018234, 0.01813, 0.018029, 0.017931, 0.017836, 0.017743, 0.017653},
                {0.01655506898,0.01638783837,0.01622748151,0.01607357236,0.01592571040,0.01578351900,0.01564664989,0.01551477751, 0.015388, 0.015265, 0.015146, 0.015032, 0.014921, 0.014813, 0.014709, 0.014608},
                {0.01400349479,0.01381599910,0.01363758573,0.01346754695,0.01330524647,0.01315010819,0.01300161149,0.01285928644, 0.012723, 0.012591, 0.012465, 0.012344, 0.012227, 0.012114, 0.012004, 0.011899},
                {0.01169364771,0.01149581822,0.01130876256,0.01113153564,0.01096329810,0.01080330908,0.01065090635,0.01050550026, 0.010367, 0.010234, 0.010106, 0.009984, 0.009867, 0.009754, 0.009645, 0.009541},
                {0.009639880884,0.009440488340,0.009253030147,0.009076367734,0.008909506536,0.008751573663,0.008601801157,0.008459509218, 0.008324, 0.008195, 0.008072, 0.007954, 0.007841, 0.007733, 0.007629, 0.00753},
                {0.007845153662,0.007651488732,0.007470403642,0.007300616598,0.007141020137,0.006990649619,0.006848663668,0.006714321849, 0.006587, 0.006466, 0.006351, 0.006241, 0.006137, 0.006037, 0.005941, 0.00585},
                {0.006302884299,0.006120606773,0.005951075453,0.005792919573,0.005644961456,0.005506185395,0.005375708131,0.005252760901,0.005137, 0.005027, 0.004923, 0.004824, 0.00473, 0.00464, 0.004555, 0.004474},
                {0.006302884299,0.006120606773,0.005951075453,0.005792919573,0.005644961456,0.005506185395,0.005375708131,0.004999024435,0.004832144086,0.004677761005,0.004534462932,0.004401045463,0.004276476136,0.004159863712,0.004050435438, 0.003948, 0.003851, 0.003759, 0.003672, 0.00359, 0.003512, 0.003438, 0.003368},
                {0.003914167128,0.003765149820,0.003628038944,0.003501424238,0.003384113665,0.003275089920,0.003173482114,0.003078536644, 0.00299, 0.002906, 0.002828, 0.002754, 0.002684, 0.002617, 0.002555, 0.002495},
                {0.01158408240,0.01092030157,0.01032543136,0.009789434555,0.009304117375,0.008862726840,0.008459649035,0.00809, 0.00775, 0.007437, 0.007147, 0.006877, 0.006627, 0.006393, 0.006175, 0.00597}
            };
            File folder = new File(".");
            String currentFileName = "";
            String curFileExt = "";
            File[] listOfFiles = folder.listFiles();
            FileWriter lilSnapFile = null;
            lilSnapFile = new FileWriter("LILSnap", false); 
            String sCurrentLine = "";
            String lastLine = "";  
            double lastLineNum = 0;
            double[] freqCtr= new double [42];
            int numSeq = 0;
            
            for (File lfile : listOfFiles) {
                if (lfile.isFile()) {    
                    currentFileName = lfile.getName();
                    curFileExt = FilenameUtils.getExtension(currentFileName);
                    if ("lil".equals(curFileExt)) {
                        BufferedReader br = new BufferedReader(new FileReader(currentFileName));
                        while ((sCurrentLine = br.readLine()) != null) 
                        {
                            lastLine = sCurrentLine;
                        }//END while
                        lilSnapFile.write(lastLine + System.getProperty("line.separator"));
                        br.close();  
                    }
                }
            }
            lilSnapFile.close();
            
            BufferedReader br = new BufferedReader(new FileReader("LILSnap"));
            while ((sCurrentLine = br.readLine()) != null)
            {
                numSeq++;
                lastLineNum = Double.parseDouble(sCurrentLine);
                if (lastLineNum >= 1) {
                    freqCtr[0]++;
                } else if (lastLineNum >= 0.95) {
                    freqCtr[1]++;
                } else if (lastLineNum >= 0.90) {
                    freqCtr[2]++;
                } else if (lastLineNum >= 0.85) {
                    freqCtr[3]++;
                } else if (lastLineNum >= 0.80) {
                    freqCtr[4]++;
                } else if (lastLineNum >= 0.75) {
                    freqCtr[5]++;
                } else if (lastLineNum >= 0.70) {
                    freqCtr[6]++;
                } else if (lastLineNum >= 0.65) {
                    freqCtr[7]++;
                } else if (lastLineNum >= 0.60) {
                    freqCtr[8]++;
                } else if (lastLineNum >= 0.55) {
                    freqCtr[9]++;
                } else if (lastLineNum >= 0.50) {
                    freqCtr[10]++;
                } else if (lastLineNum >= 0.45) {
                    freqCtr[11]++;
                } else if (lastLineNum >= 0.40) {
                    freqCtr[12]++;
                } else if (lastLineNum >= 0.35) {
                    freqCtr[13]++;
                } else if (lastLineNum >= 0.30) {
                    freqCtr[14]++;
                } else if (lastLineNum >= 0.25) {
                    freqCtr[15]++;
                } else if (lastLineNum >= 0.20) {
                    freqCtr[16]++;
                } else if (lastLineNum >= 0.15) {
                    freqCtr[17]++;
                } else if (lastLineNum >= 0.10) {
                    freqCtr[18]++;
                } else if (lastLineNum >= 0.05) {
                    freqCtr[19]++;
                } else if (lastLineNum >= 0.00) {
                    freqCtr[20]++;
                } else if (lastLineNum >= -0.05) {
                    freqCtr[21]++;
                } else if (lastLineNum >= -0.10) {
                    freqCtr[22]++;
                } else if (lastLineNum >= -0.15) {
                    freqCtr[23]++;
                } else if (lastLineNum >= -0.20) {
                    freqCtr[24]++;
                } else if (lastLineNum >= -0.25) {
                    freqCtr[25]++;
                } else if (lastLineNum >= -0.30) {
                    freqCtr[26]++;
                } else if (lastLineNum >= -0.35) {
                    freqCtr[27]++;
                } else if (lastLineNum >= -0.40) {
                    freqCtr[28]++;
                } else if (lastLineNum >= -0.45) {
                    freqCtr[29]++;
                } else if (lastLineNum >= -0.50) {
                    freqCtr[30]++;
                } else if (lastLineNum >= -0.55) {
                    freqCtr[31]++;
                } else if (lastLineNum >= -0.60) {
                    freqCtr[32]++;
                } else if (lastLineNum >= -0.65) {
                    freqCtr[33]++;
                } else if (lastLineNum >= -0.70) {
                    freqCtr[34]++;
                } else if (lastLineNum >= -0.75) {
                    freqCtr[35]++;
                } else if (lastLineNum >= -0.80) {
                    freqCtr[36]++;
                } else if (lastLineNum >= -0.85) {
                    freqCtr[37]++;
                } else if (lastLineNum >= -0.90) {
                    freqCtr[38]++;
                } else if (lastLineNum >= -0.95) {
                    freqCtr[39]++;
                } else if (lastLineNum >= -1.00) {
                    freqCtr[40]++;
                } else {
                    freqCtr[41]++;
                }
            
            }//end while
            br.close();
            for (int i=0; i<42; i++) {
                freqCtr[i] = freqCtr[i]/numSeq;                        
            }
            
            //now we calculate the total variantion
            double totalV =0;
            for (int i=0; i<42; i++) {
                if (freqCtr[i]>snapProb[i][alephVal])
                {
                    totalV = totalV+freqCtr[i]-snapProb[i][alephVal];
                }
            }
            double expTotalV = 4.6983*Math.pow(numSeq, -0.57);
            
            
            //now we calculate the Hellinger distance distance
            double hellingerD =0;
            double temp = 0;
            for (int i=0; i<42; i++) {
                temp = sqrt(freqCtr[i])-sqrt(snapProb[i][alephVal]);
                hellingerD = hellingerD + temp*temp;
            }
            hellingerD = sqrt(hellingerD)/sqrt(2);
            double expHellingerD = 3.3809*Math.pow(numSeq, -0.541);
            
            
            //now we calculate the root-mean-square deviation
            double rmsdV =0;
            for (int i=0; i<42; i++) {
                temp = freqCtr[i]-snapProb[i][alephVal];
                rmsdV = rmsdV+temp*temp;
            }
            rmsdV = sqrt(rmsdV/42);
            double expRMSDv = 0.3404*Math.pow(numSeq, -0.583);
            
            
            String [] normalCurveY ={
                "y=2.270650153*dnorm(2.270650153*x,mean=0,sd=1);", //n=2^19 (64KB sequence) aleph = 0
                "y=2.293128584*dnorm(2.293128584*x,mean=0,sd=1);", //n=2^20 (128KB sequence) aleph = 1
                "y=2.314307463*dnorm(2.314307463*x,mean=0,sd=1);", //n=2^21 (256KB sequence) aleph = 2
                "y=2.334321970*dnorm(2.334321970*x,mean=0,sd=1);", //n=2^22 (512KB sequence) aleph = 3
                "y=2.353287612*dnorm(2.353287612*x,mean=0,sd=1);", //n=2^23 (1MB sequence) aleph = 4
                "y=2.371303822*dnorm(2.371303822*x,mean=0,sd=1);", //n=2^24 (2MB sequence)  aleph = 5
                "y=2.388456784*dnorm(2.388456784*x,mean=0,sd=1);", //n=2^25 (4MB sequence)  aleph = 6
                "y=2.404821663*dnorm(2.404821663*x,mean=0,sd=1);", //n=2^26 (8MB sequence) aleph = 7
                "y=2.420464395*dnorm(2.420464395*x,mean=0,sd=1);", //n=2^27 (16MB sequence) aleph = 8
                "y=2.435443117*dnorm(2.435443117*x,mean=0,sd=1);", //n=2^28 (32MB sequence) aleph = 9
                "y=2.449809342*dnorm(2.449809342*x,mean=0,sd=1);", //n=2^29 (64MB sequence) aleph = 10
                "y=2.463608921*dnorm(2.463608921*x,mean=0,sd=1);", //n=2^30 (128MB sequence) aleph = 11
                "y=2.476882832*dnorm(2.476882832*x,mean=0,sd=1);", //n=2^31 (256MB sequence) aleph = 12
                "y=2.489667841*dnorm(2.489667841*x,mean=0,sd=1);", //n=2^32 (512MB sequence) aleph = 13
                "y=2.501997058*dnorm(2.501997058*x,mean=0,sd=1);", //n=2^33 (1GB sequence) aleph = 14
                "y=2.513900396*dnorm(2.513900396*x,mean=0,sd=1);"  //n=2^24 (2GB sequence) aleph = 15
            };
            String prepareDraw = "rx <- range(x <- c(-2,2)); ry <- range(y <- c(0,1.3)); "
                    + "plot(x, y, type=\"n\", xlab=\"\", ylab=\"\");";
            
            String normXY = "x<-seq(-0.975,0.975,by=0.05); y<-c(";
            for (int i=1; i<40; i++) {
                normXY += snapProb[i][alephVal]/0.05+",";
            }
            normXY += snapProb[40][alephVal]/0.05;
            normXY +=");lines(x,y,type=\"l\",lwd=0.3,col=\"black\");";

            String realXY = "x<-seq(-0.975,0.975,by=0.05); y<-c(";
            for (int i=1; i<40; i++) {
                realXY += freqCtr[i]/0.05+",";
            }
            realXY += freqCtr[40]/0.05;
            realXY +="); lines(x,y,type=\"l\",lwd=0.3,col=\"blue\");";
            
            String legend = "legend(\"topright\",  c(\"expected\", \"actual\"), cex=0.5, col=c(\"black\",\"blue\"), lty=1:1, bty=\"n\");";
            
            //System.out.print(normXY);
            
            RCaller caller = new RCaller(); 
            caller.setRscriptExecutable(rScriptPath);
            caller.cleanRCode();
            rcaller.RCode code = new rcaller.RCode();
            caller.setRCode(code);
            code.clear();
            code.addRCode("pdf(\'"+snapFilename+"\', height=3.5, width=5);");
            code.addRCode(prepareDraw);
            code.addRCode("x=seq(-2,2,length=200);");
            code.addRCode(normalCurveY[alephVal]);
            code.addRCode("lines(x,y,type=\"l\",lwd=0.3,col=\"black\");");
            code.addRCode(realXY);
            code.addRCode(normXY);
            code.addRCode(legend);
            code.addRCode("dev.off()");
            code.endPlot();
            caller.runOnly();
            int sequenceLen = (int)Math.pow(2, alephVal)*64;
            rFile.write("\nYou have requested to carry out the snapShot testing. I generated\n"
                    + "the snapShot curve in the file: "+snapFilename+". The statistical\n"
                    + "distances are as follows (if the actual value is smaller than the expeted\n"
                    + "value, then your sequence passed the LIL test):\n"
                    + "Total Variation: \n\t\t" + expTotalV + "(expected)\t\n\t\t"
                    + totalV + " (actual)\n"
                    + "Hellinger distance: \n\t\t" + expHellingerD + "(expected)\t\n\t\t"
                    + hellingerD + " (actual)\n"
                    + "root-mean-square deviation: \n\t\t"+ expRMSDv + "(expected)\t\n\t\t"
                    + rmsdV + " (actual)\n\n"
                    + "The LIL testing was carried out on "+ numSeq +" sequences. Each Sequence is\n"
                    + sequenceLen+ "KB long.\n");
            
        }
        catch (Exception e) 
        {
            System.out.println(e.toString());
        }
    }
} // end LILtest class
