#include "undo_redo_helper.h"

void gpupixel::UndoRedoHelper::addRecord(const gpupixel::OpenPSRecord &record) {
  int lastIndex = recordList.size() - 1;
  if (currentIndex < lastIndex) {
    for (int i = currentIndex + 1; i <= lastIndex; i++) {
      recordList.pop_back();
    }
  }
  if (recordList.empty()) {
    recordList.emplace_back(record);
    Util::Log("UndoRedoHelper", "addRecord {%s}", record.toString().c_str());
  } else {
    auto currentLatestRecord = recordList.back();
    if (!record.equals(currentLatestRecord)) {
      recordList.emplace_back(record);
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

gpupixel::OpenPSRecord gpupixel::UndoRedoHelper::undo() {
  if (canUndo()) {
    currentIndex--;
    return recordList[currentIndex];
  }
  return getEmptyRecord();
}

gpupixel::OpenPSRecord gpupixel::UndoRedoHelper::redo() {
  if (canRedo()) {
    currentIndex++;
    return recordList[currentIndex];
  }
  return getEmptyRecord();
}

void gpupixel::UndoRedoHelper::addEmptyRecord() {
  addRecord(getEmptyRecord());
}

gpupixel::UndoRedoHelper::UndoRedoHelper() {
  addEmptyRecord();
}

gpupixel::OpenPSRecord gpupixel::UndoRedoHelper::getEmptyRecord() {
  return gpupixel::OpenPSRecord(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
}
