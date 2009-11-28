/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package yui.classes.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import yui.classes.Combo;

/**
 *
 * @author leo
 */
public class ComboServlet  extends  HttpServlet{

    @Override
      public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {

        Combo combo = new Combo(request,response);


    PrintWriter out = response.getWriter();
    out.println(combo.getRaw());



  }
        @Override
      public void doPost(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {
                        // TODO only get supported
            doGet(request,response);
  }


}
