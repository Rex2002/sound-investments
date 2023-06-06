package dataRepo.api;

import java.net.http.HttpResponse;

@FunctionalInterface
public interface ErrHandler {
	HttpResponse<String> handle(HttpResponse<String> res) throws APIErr;
}