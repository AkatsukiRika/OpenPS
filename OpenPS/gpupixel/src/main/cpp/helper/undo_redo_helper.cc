#include "undo_redo_helper.h"
#include "openps_record.h"

USING_NS_GPUPIXEL

void gpupixel::UndoRedoHelper::addRecord(const AbstractRecord& record) {
  int lastIndex = recordList.size() - 1;
  if (currentIndex < lastIndex) {
    recordList.erase(recordList.begin() + currentIndex + 1, recordList.end());
  }
  if (recordList.empty()) {
    recordList.emplace_back(std::shared_ptr<gpupixel::AbstractRecord>(record.clone()));
    Util::Log("UndoRedoHelper", "addRecord {%s}", record.toString().c_str());
  } else {
    auto currentLatestRecord = recordList.back();
    if (!record.equals(*currentLatestRecord)) {
      recordList.emplace_back(std::shared_ptr<gpupixel::AbstractRecord>(record.clone()));
      Util::Log("UndoRedoHelper", "addRecord {%s}", record.toString().c_str());
    }
  }
  currentIndex = recordList.size() - 1;
  Util::Log("UndoRedoHelper", "currentIndex: %d", currentIndex);
}

bool gpupixel::UndoRedoHelper::canUndo() {
  int recordCount = recordList.size();
  if (recordCount == 0) {
    return false;
  }
  return currentIndex > 0;
}

bool gpupixel::UndoRedoHelper::canRedo() {
  int recordCount = recordList.size();
  int lastIndex = recordCount - 1;
  if (recordCount == 0) {
    return false;
  }
  return currentIndex < lastIndex;
}

std::shared_ptr<AbstractRecord> gpupixel::UndoRedoHelper::undo() {
  if (canUndo()) {
    currentIndex--;
    return recordList[currentIndex];
  }
  return getEmptyRecord();
}

std::shared_ptr<AbstractRecord> gpupixel::UndoRedoHelper::redo() {
  if (canRedo()) {
    currentIndex++;
    return recordList[currentIndex];
  }
  return getEmptyRecord();
}

void gpupixel::UndoRedoHelper::addEmptyRecord() {
  addRecord(*getEmptyRecord());
}

gpupixel::UndoRedoHelper::UndoRedoHelper() {
  addEmptyRecord();
}

std::shared_ptr<AbstractRecord> gpupixel::UndoRedoHelper::getEmptyRecord() {
  return std::make_shared<OpenPSRecord>(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
}
