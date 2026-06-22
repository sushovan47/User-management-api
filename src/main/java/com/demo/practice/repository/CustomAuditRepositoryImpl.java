package com.demo.practice.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Component;

import com.demo.practice.entity.User;
import com.demo.practice.entity.UserCredentials;
import com.demo.practice.entity.UserPswdResetToken;
import com.demo.practice.model.AuditLogDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class CustomAuditRepositoryImpl implements CustomAuditRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@SuppressWarnings("unchecked")
	public List<AuditLogDto> getUserHistoryLog(Long userId) {
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd - HH:mm");

		Set<Number> allRevisions = new HashSet<>();

		// 1. Gather all historical transaction numbers using lightweight projection
		// tracking queries
		try {
			allRevisions.addAll(auditReader.getRevisions(User.class, userId));
		} catch (Exception e) {
		}

		try {
			List<Number> credRevs = auditReader.createQuery().forRevisionsOfEntity(UserCredentials.class, false, true)
					.add(AuditEntity.relatedId("user").eq(userId)).addProjection(AuditEntity.revisionNumber())
					.getResultList();
			allRevisions.addAll(credRevs);
		} catch (Exception e) {
		}

		try {
			List<Number> tokenRevs = auditReader.createQuery()
					.forRevisionsOfEntity(UserPswdResetToken.class, false, true)
					.add(AuditEntity.relatedId("user").eq(userId)).addProjection(AuditEntity.revisionNumber())
					.getResultList();
			allRevisions.addAll(tokenRevs);
		} catch (Exception e) {
		}

		if (allRevisions.isEmpty()) {
			return Collections.emptyList();
		}

		// 2. Sort the revisions array descending (Newest first)
		List<Number> sortedRevisions = new ArrayList<>(allRevisions);
		sortedRevisions.sort(Collections.reverseOrder());

		// Identify the oldest revision ID (the registration event) safely without
		// loading any entities
		Number oldestRevisionId = sortedRevisions.get(sortedRevisions.size() - 1);

		List<AuditLogDto> auditLogs = new ArrayList<>();
		long logCounter = sortedRevisions.size();

		// 3. Process records using lightweight counts, eliminating StackOverflowError
		// completely
		for (Number rev : sortedRevisions) {
			Date revDate = auditReader.getRevisionDate(rev);
			String formattedDate = dateFormat.format(revDate);
//			String actionDescription = "Profile metadata details updated successfully";
			String actionDescription = "";

			// A: Check if this revision is the initial registration step
			if (Objects.equals(rev, oldestRevisionId)) {
				actionDescription = "Logged in successfully / Profile Registered";
			} else {
				boolean isCredChange = false;

				try {
					Long count = (Long) auditReader.createQuery().forEntitiesAtRevision(User.class, rev)
							.add(AuditEntity.relatedId("user").eq(userId)).addProjection(AuditEntity.id().count())
							.getSingleResult();
					if (count > 0) {

						actionDescription = "Profile metadata details updated successfully";
						isCredChange = true;

					}
				} catch (Exception e) {
				}

				// B: Check Credentials table alterations using pure count query projections
				if (!isCredChange) {
					try {
						Long count = (Long) auditReader.createQuery().forEntitiesAtRevision(UserCredentials.class, rev)
								.add(AuditEntity.relatedId("user").eq(userId)).addProjection(AuditEntity.id().count())
								.getSingleResult();
						if (count > 0) {

							actionDescription = "Password changed successfully";
							isCredChange = true;

						}
					} catch (Exception e) {
					}
				}

				// C: Check Token table alterations using count query projections
				if (!isCredChange) {
					try {
						Long count = (Long) auditReader.createQuery()
								.forEntitiesAtRevision(UserPswdResetToken.class, rev)
								.add(AuditEntity.relatedId("user").eq(userId)).addProjection(AuditEntity.id().count())
								.getSingleResult();
						if (count > 0) {
							actionDescription = "Password reset token requested successfully";
						}
					} catch (Exception e) {
					}
				}
			}

			auditLogs.add(new AuditLogDto(logCounter--, rev.longValue(), formattedDate, actionDescription));
		}

		return auditLogs;
	}

}
