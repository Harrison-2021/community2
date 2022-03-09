package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;

    // Controller与浏览器交互，测试Tomcat服务器
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {

        return "Hello Spring Boot.";
    }

    // 测试三层架构交互
    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    // 测试接收请求和响应的底层实现
    @RequestMapping("/http")
    public void http(HttpServletRequest request,
                     HttpServletResponse response) {
        // 获取请求数据
        System.out.println(request.getMethod()); // 请求方法
        System.out.println(request.getServletPath()); // 请求路径
        Enumeration<String> enumeration = request.getHeaderNames(); // 请求头中的key
        while(enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println(request.getParameter("code")); // 请求路径后带的参数?code=123

        // 返回响应数据
        response.setContentType("text/html;charset=utf-8");
        try ( // jdk7新语法，写在括号内，自动close
                PrintWriter writer = response.getWriter();
        ){
            writer.write("<h1>响应的数据</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 框架中简单的操作，框架已经将底层操作包装好了
    // 测试get请求
    // /students?current=1&limit=20
    @RequestMapping(path = "/students", method = {RequestMethod.GET})
    @ResponseBody
    public String students(@RequestParam(value = "current", required = false, defaultValue = "1") int current,
                           @RequestParam(value = "limit", defaultValue = "10") int limit) {
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }
    // /student/123
    @RequestMapping(path="/student/{id}", method = {RequestMethod.GET})
    @ResponseBody
    public String student(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }

    // post请求 /student
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String addStudent(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "Save success";
    }

    //  测试响应
    //  一、响应HTML数据，两种方法
    // 方法一、ModelAndView
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView modelAndView = new ModelAndView(); // model和视图名封装成一个对象
        modelAndView.addObject("name", "张三");
        modelAndView.addObject("age", "43");
        modelAndView.setViewName("/demo/view"); // 要处理的动态模板文件
        return modelAndView;
    }

    // 方法二、Model
    @RequestMapping(path = "school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        model.addAttribute("name", "沈阳建筑大学");
        model.addAttribute("age", "100");
        return "/demo/view";
    }

    //    二、响应JSON数据
    // 返回一个map对象
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody // 如果想返回js对象，必须有此注解
    public Map<String, Object> getEmp(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", "20");
        map.put("sal", "8000");
        return map;
    }

    // 返回一个List对象
    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> emp1 = new HashMap<>();
        emp1.put("name", "张三");
        emp1.put("age", "20");
        emp1.put("sal", "8000");
        list.add(emp1);

        Map<String, Object> emp2 = new HashMap<>();
        emp2.put("name", "李四");
        emp2.put("age", "21");
        emp2.put("sal", "8000");
        list.add(emp2);

        Map<String, Object> emp3 = new HashMap<>();
        emp3.put("name", "王二");
        emp3.put("age", "22");
        emp3.put("sal", "8000");
        list.add(emp3);

        return list;
    }

    // Cookie测试
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        // 创建Cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());

        // 设置Cookie
        // 哪些路径下的请求可以有此Cookie凭证
        cookie.setPath("/community/alpha");
        // cookie生存时间，也是存储位置,10分钟
        cookie.setMaxAge(60 * 10);

        // 发送Cookie,添加进响应头，就会自动发送给浏览器
        response.addCookie(cookie);
        return "set Cookie";
    }

    // 测试请求中携带cookie
    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) {
        // 服务端获取到指定的cookie，有多种处理方式，以打印到输出台为例
        System.out.println(code);
        return "get cookie";
    }

    // 测试session
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("id", 100);
        session.setAttribute("name", "test");
        return "set session";
    }

    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }

    // ajax示例
    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name); // 可以获取异步请求中的js对象参数值
        System.out.println(age);
        return CommunityUtil.getJSONString(0, "操作成功!");
    }
}
