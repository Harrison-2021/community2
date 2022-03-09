$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");	// 点击发布按钮后，弹出框关闭，自动访问服务器处理请求

	// 通过id属性获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	// 发送异步请求(POST)
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title, "content":content},
		function (data) {	// 回调函数，目的是将提示信息msg在提示框展现出来
			data = $.parseJSON(data);		// 转为js对象，方便取值
			$("hintBody").text(data.msg);	// 将msg值放入提示框中的显示框中
			$("#hintModal").modal("show");		// 提示框展现提示信息
			setTimeout(function(){		// 提示框展现时间设定，2秒后自动隐藏
				$("#hintModal").modal("hide");
				// 刷新页面
				if(data.code == 0) {
					window.location.reload();
				}
			}, 2000);
		}
	)
}