$(function(){
    $("#topBtn").click(setTop);
    $("#usefulBtn").click(setUseful);
    $("#deleteBtn").click(setDelete);
});

function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1?"Liked" : "Like");
            } else {
                alert(data.msg);
            }
        }
    )
}

function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id": $("#postId").val()},
        function(data){
            data = $.parseJSON(data);
            if (data.code == 0) {
                // when the btn is clicked, disable the btn
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

function setUseful() {
    $.post(
        CONTEXT_PATH + "/discuss/useful",
        {"id": $("#postId").val()},
        function(data){
            data = $.parseJSON(data);
            if (data.code == 0) {
                // when the btn is clicked, disable the btn
                $("#usefulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id": $("#postId").val()},
        function(data){
            data = $.parseJSON(data);
            if (data.code == 0) {
                // when the post is deleted, return to home page
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}

