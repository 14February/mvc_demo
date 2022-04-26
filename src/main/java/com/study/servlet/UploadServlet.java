package com.study.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;

@WebServlet(urlPatterns = "/servlet/upload")
@MultipartConfig(location = "upload", maxFileSize = 1024 * 1024 * 20)
public class UploadServlet extends HttpServlet {

    public static final String UPLOAD_DIRECTORY = "upload";
    private static final long serialVersionUID = 1L;

    private static MultipartConfig config
            = UploadServlet.class.getAnnotation(MultipartConfig.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Part part = null;

        try{
            part = req.getPart("uploadFile");//获取 part 用于处理上传的文件
        } catch(IllegalStateException ise) {
            //上传的单个文件超出 maxFileSize 或者上传的总的数据量超出 maxreqSize 时会抛出此异常

            //如果注解中没设置此项，那就是单个文件超出限制
//            if(config.maxreqSize() == -1L)
//                req.setAttribute("message","错误信息: 单个文件超限");
//            else
            if(config.maxFileSize() == -1L)
                //如果注解中没有设置单个文件最大限制，那就是总数据量超限。
                req.setAttribute("message","错误信息: 总数据量超限");
            else
                req.setAttribute("message","错误信息: Error");

            // 跳转到 message.jsp
            req.getServletContext().getRequestDispatcher("/message.jsp").forward(
                    req, resp);
        }

        if(part == null) return;

        //获得上传的文件名，没有判断用户没有选择文件直接提交的情况
        //没有判断上传文件失败的情况
        String fileName = part.getSubmittedFileName();
        String message = "";
        message += "<p>contentType : " + part.getContentType() + "</p>";
        message += "<p>fileName : " + fileName + "</p>";
        message += "<p>fileSize : " + part.getSize() + "</p>";
        message += "<p>header names :<br/>";
        for(String headerName : part.getHeaderNames())
            message += headerName + " : " + part.getHeader(headerName) + "<br/>";

        message += "</p>";

        //为了避免文件重名，将时间组合到了文件名中。实际项目中可以考虑使用用户主键或者生成一个唯一的ID来组合文件名。
        String saveName = System.currentTimeMillis() + "_" + fileName;

        // 构造临时路径来存储上传的文件
        // 这个路径相对当前应用的目录
        String uploadPath = req.getServletContext().getRealPath("./") + File.separator + UPLOAD_DIRECTORY;

        // 如果目录不存在则创建
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }


        String fullPath = uploadPath + File.separator + saveName;

        //part.write(fullPath);
        //将上传的文件保存到磁盘，默认是注解中location的相对地址，也可以传入一个绝对路径

        InputStream in = part.getInputStream();
        OutputStream out = new FileOutputStream(fullPath);
        byte[] buffer = new byte[1024];
        int length = -1;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.close();

        System.out.println(fullPath);

        message += "<p>文件上传成功!</p>";
        message += "<p><img src=\"/servlet/upload/" + saveName + "\"/></p>";

        req.setAttribute("message",message);
        // 跳转到 message.jsp
        req.getServletContext().getRequestDispatcher("/message.jsp").forward(req, resp);

    }
    
}
