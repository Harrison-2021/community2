$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) { // 关注按钮
		// 关注TA
		$.post(
			CONTEXT_PATH + "/follow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			function (data) {
				data = $.parseJSON(data);
				if(data.code == 0) {
					window.location.reload();	// 为了完整显示当前个人主页数据，需要刷新页面，其他网页关注可以不必刷新
				} else {
					alert(data.msg);
				}
			}
		);
	} else {
		// 取消关注
		$.post(
			CONTEXT_PATH + "/unfollow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			function (data) {
				data = $.parseJSON(data);
				if(data.code == 0) {
					window.location.reload();
				} else {
					alert(data.msg);
				}
			}
		);
	}
}