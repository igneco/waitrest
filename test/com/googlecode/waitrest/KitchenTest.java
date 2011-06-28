package com.googlecode.waitrest;

import com.googlecode.totallylazy.Option;
import com.googlecode.utterlyidle.Response;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.RequestBuilder.get;
import static com.googlecode.utterlyidle.RequestBuilder.put;
import static com.googlecode.utterlyidle.Responses.response;
import static com.googlecode.utterlyidle.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class KitchenTest {
    private Kitchen kitchen = new Kitchen();

    @Test
    public void shouldRecordBasicInteraction() {
        kitchen.receiveOrder(get("/test").build(), response(OK).entity("test entity"));

        assertThat(kitchen.serve(get("/test").build()).get(), is(response(OK).entity("test entity")));
    }

    @Test
    public void shouldOverrideRequests() {
        kitchen.receiveOrder(get("/test").build(), response(OK).entity("test entity"));
        kitchen.receiveOrder(get("/test").build(), response(OK).entity("new test entity"));

        assertThat(kitchen.serve(get("/test").build()).get(), is(response(OK).entity("new test entity")));
    }

    @Test
    public void shouldIgnoreExtraQueryParams() {
        kitchen.receiveOrder(get("/test").build(), response(OK).entity("test entity"));
        assertThat(kitchen.serve(get("/test?param=ignore").build()).get(), is(response(OK).entity("test entity")));
    }

    @Test
    public void shouldReturnNoneResponseMatches() {
        assertThat(kitchen.serve(get("/test?param=ignore").build()), CoreMatchers.<Option<Response>>is(none(Response.class)));
    }

    @Test
    public void shouldReturnAResponse() {
        String responseContent = "bar";
        kitchen.receiveOrder(put("/foo").withInput(responseContent.getBytes()).build());
        Response response = kitchen.serve(get("/foo").build()).get();
        assertThat(new String(response.bytes()), Matchers.is(responseContent));
    }

    @Test
    public void shouldPreserveContentTypeOfResponse() {
        String contentType = "text/plain";
        kitchen.receiveOrder(put("/foo").withHeader(CONTENT_TYPE, contentType).withInput("bar".getBytes()).build());
        Response response = kitchen.serve(get("/foo").build()).get();
        assertThat(response.header(CONTENT_TYPE), Matchers.is(contentType));

    }

    @Test
    public void shouldSupportMatchingOnQueryParams() {
        kitchen.receiveOrder(put("/foo?bar=dan").withInput("dan".getBytes()).build());
        kitchen.receiveOrder(put("/foo?bar=tom").withInput("tom".getBytes()).build());
        Response response = kitchen.serve(get("/foo?ignore=me&bar=tom").build()).get();
        assertThat(new String(response.bytes()), Matchers.is("tom"));
    }
}
