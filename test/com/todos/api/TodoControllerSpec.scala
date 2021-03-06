package com.todos.api

import java.util.UUID

import com.todos.json.Serializers
import com.todos.model.{Comment, Todo}
import com.todos.service.TodoService
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class TodoControllerSpec extends PlaySpec with MockitoSugar with Serializers {

  "TodoController" should {

    "return the list of todos" in {
      val todoService = mock[TodoService]
      val controller = new TodoController(stubControllerComponents(), todoService)


      val todo1 = Todo(id = UUID.randomUUID(), title = "Title 1", completed = false, comments = List.empty)
      val todo2 = Todo(id = UUID.randomUUID(), title = "Title 2", completed = false, comments = List.empty)
      val todos = List(todo1, todo2)
      when(todoService.getTodos()) thenReturn Future.successful(todos)

      val response: Future[Result] = controller.getTodos().apply(FakeRequest(GET, "/todos"))
      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      val result = contentAsJson(response).as[List[Todo]]
      result mustBe todos
    }

    "return newly created todo" in {
      val todoService = mock[TodoService]
      val controller = new TodoController(stubControllerComponents(), todoService)

      val todo = Todo(id = UUID.randomUUID(), title = "Title 1", completed = false, comments = List.empty)
      when(todoService.createTodo(any[CreateTodoCommand])) thenReturn Future.successful(todo)

      val request: Request[CreateTodoCommand] = FakeRequest(
        method = POST,
        uri = "/todos",
        headers = FakeHeaders(),
        body = CreateTodoCommand(title = "title")
      )

      val response: Future[Result] = controller.createTodo().apply(request)
      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      val result = contentAsJson(response).as[Todo]
      result mustBe todo
    }

    "return edited todo" in {
      val todoService = mock[TodoService]
      val controller = new TodoController(stubControllerComponents(), todoService)

      val todo = Todo(id = UUID.randomUUID(), title = "Title 1", completed = false, comments = List.empty)
      when(todoService.editTodo(any[EditTodoCommand])) thenReturn Future.successful(todo)

      val request: Request[EditTodoCommand] = FakeRequest(
        method = POST,
        uri = "/todos",
        headers = FakeHeaders(),
        body = EditTodoCommand(id= UUID.randomUUID(), title = "title", completed= false, comments = List.empty[EditCommentCommand])
      )

      val response: Future[Result] = controller.editTodo().apply(request)
      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      val result = contentAsJson(response).as[Todo]
      result mustBe todo
    }

    "delete todo" in {
      val todoService = mock[TodoService]
      val controller = new TodoController(stubControllerComponents(), todoService)
      val todoId = UUID.randomUUID()
      when(todoService.deleteTodo(todoId)) thenReturn Future.unit

      val response: Future[Result] = controller.deleteTodo(todoId).apply(FakeRequest(DELETE, s"/todos/${todoId.toString}"))
      status(response) mustBe OK
    }

    "mark todo complete true" in {
      val todoService = mock[TodoService]
      val controller = new TodoController(stubControllerComponents(), todoService)
      val todoId = UUID.randomUUID()

      when(todoService.updateCompleteFlag(todoId, true)) thenReturn Future.unit

      val request: Request[UpdateCompleteFlagCommand] = FakeRequest(
        method = PUT,
        uri = s"/todos/${todoId.toString}/complete",
        headers = FakeHeaders(),
        body = UpdateCompleteFlagCommand(true)
      )

      val response: Future[Result] = controller.updateCompleteFlag(todoId).apply(request)
      status(response) mustBe OK
    }

    "get todo should return 404" in {
      val todoService = mock[TodoService]
      val controller = new TodoController(stubControllerComponents(), todoService)
      val todoId = UUID.randomUUID()

      when(todoService.findById(todoId)) thenReturn Future.successful(None)

      val request = FakeRequest(GET, s"/todos/${todoId}")
      val response: Future[Result] = controller.getTodo(todoId).apply(request)
      status(response) mustBe NOT_FOUND
    }

    "mark todo complete false" in {
      val todoService = mock[TodoService]
      val controller = new TodoController(stubControllerComponents(), todoService)
      val todoId = UUID.randomUUID()
      when(todoService.updateCompleteFlag(todoId, false)) thenReturn Future.unit

      val request: Request[UpdateCompleteFlagCommand] = FakeRequest(
        method = PUT,
        uri = s"/todos/${todoId.toString}/complete",
        headers = FakeHeaders(),
        body = UpdateCompleteFlagCommand(false)
      )

      val response: Future[Result] = controller.updateCompleteFlag(todoId).apply(request)
      status(response) mustBe OK
    }


    "add todo comment" in {
      val todoService = mock[TodoService]
      val controller = new TodoController(stubControllerComponents(), todoService)

      val todoId = UUID.randomUUID()
      val todo = Todo(
        id = todoId,
        title = "Todo title",
        completed = false,
        comments = List(Comment(id = UUID.randomUUID(), content = "new comment"))
      )
      when(todoService.addComment(todoId, AddCommentCommand("new comment"))) thenReturn Future.successful(todo)

      val request: Request[AddCommentCommand] = FakeRequest(
        method = POST,
        uri = s"/todos/${todoId.toString}/comment",
        headers = FakeHeaders(),
        body = AddCommentCommand("new comment")
      )

      val response: Future[Result] = controller.addComment(todoId).apply(request)
      status(response) mustBe OK
      val result = contentAsJson(response).as[Todo]
      result mustBe todo
    }
  }
}
