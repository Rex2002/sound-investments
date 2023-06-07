package dataRepo.api;

import java.net.http.HttpResponse;

@FunctionalInterface
public interface ErrHandler {
	public HttpResponse<String> handle(HttpResponse<String> res, APIReq api) throws APIErr;
}
