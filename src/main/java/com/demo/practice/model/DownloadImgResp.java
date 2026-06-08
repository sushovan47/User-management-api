package com.demo.practice.model;

import org.springframework.core.io.Resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DownloadImgResp {

	private String fileName;
	private Resource fileResource;
	private String message;
	private boolean success;

}
