$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	// 发布后自动隐藏
	$("#sendModal").modal("hide");

	// 获取页面写入的数据
	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();

	// 发送异步请求(POST)
	$.post(
		// 地址，即controller接受的请求地址
		CONTEXT_PATH + "/letter/send",
		// json格式将页面写入的内容写给controller
		{"toName":toName, "content":content},
		// 回调函数，转为json对象，json接受服务端处理后的信息，进行页面的处理
		function (data) {
			// 将返回的字符串转为json对象，方便调用
			data = $.parseJSON(data);
			// 在提示框中显示返回消息
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");
			// 2秒后自动隐藏，隐藏后刷新页面
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 发布成功，就刷新页面
				if(data.code == 0) {
					window.location.reload();
				}
			}, 2000);
		}
	)

}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}